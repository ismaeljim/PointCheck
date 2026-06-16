package com.duoc.app.features.reservation.service

import com.duoc.app.features.professionalprofile.repository.ProfessionalProfileRepository
import com.duoc.app.features.reservation.dto.AvailabilityResponse
import com.duoc.app.features.reservation.dto.ReservationRequest
import com.duoc.app.features.reservation.dto.ReservationResponse
import com.duoc.app.features.reservation.model.Reservation
import com.duoc.app.features.reservation.model.ReservationStatus
import com.duoc.app.features.reservation.repository.ReservationRepository
import com.duoc.app.features.service.model.ServiceOffering
import com.duoc.app.features.service.repository.ServiceOfferingRepository
import com.duoc.app.features.user.repository.UserRepository
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Motor de Reservas y Disponibilidad.
 *
 * Este componente es el núcleo del negocio. Gestiona la intersección entre
 * la agenda del especialista y las necesidades del cliente. Se encarga de
 * calcular horarios disponibles, gestionar el ciclo de vida de las citas
 * y coordinar con el sistema de notificaciones y facturación.
 */
@Service
class ReservationService(
    private val reservationRepository: ReservationRepository,
    private val userRepository: UserRepository,
    private val serviceOfferingRepository: ServiceOfferingRepository,
    private val professionalProfileRepository: ProfessionalProfileRepository,
    private val notificationService: com.duoc.app.features.notification.service.NotificationService,
    private val billingRecordRepository: com.duoc.app.features.billing.repository.BillingRecordRepository,
    private val auditLogger: com.duoc.app.core.audit.AuditLogger
) {

    private val objectMapper = jacksonObjectMapper()

    /**
     * Calcula los bloques de tiempo disponibles para un especialista en una fecha específica.
     *
     * La lógica incluye:
     * - Normalización de días de la semana (soporte multi-idioma para configuración JSON).
     * - Validación de horas de trabajo configuradas en el perfil profesional.
     * - Exclusión de bloques ya reservados o en conflicto.
     * - Cálculo dinámico basado en la duración predeterminada de la sesión.
     *
     * @param specialistId ID del especialista o del usuario asociado.
     * @param date Fecha para la cual se consulta la disponibilidad.
     * @return [AvailabilityResponse] con la lista de horarios (HH:mm) disponibles.
     */
    fun getAvailability(specialistId: String, date: LocalDate): AvailabilityResponse {
        val profile = professionalProfileRepository.findById(specialistId)
            .orElseGet { professionalProfileRepository.findByUser_Id(specialistId) }
            ?: throw IllegalArgumentException("Perfil profesional no encontrado para el especialista.")

        val actualUserId = profile.user.id!!

        val workingHours = try {
            if (!profile.workingHoursJson.isNullOrBlank()) {
                val rawMap: Map<String, Any> = objectMapper.readValue(profile.workingHoursJson!!)
                rawMap.mapKeys { it.key.uppercase() }
            } else {
                emptyMap()
            }
        } catch (_: Exception) {
            emptyMap()
        }

        fun String.normalize(): String {
            val original = listOf("Á", "É", "Í", "Ó", "Ú", "Ñ")
            val normalized = listOf("A", "E", "I", "O", "U", "N")
            var res = this.uppercase()
            original.forEachIndexed { i, s -> res = res.replace(s, normalized[i]) }
            return res
        }

        val dayTranslations = mapOf(
            "MONDAY" to listOf("MONDAY", "LUNES"),
            "TUESDAY" to listOf("TUESDAY", "MARTES"),
            "WEDNESDAY" to listOf("WEDNESDAY", "MIERCOLES"),
            "THURSDAY" to listOf("THURSDAY", "JUEVES"),
            "FRIDAY" to listOf("FRIDAY", "VIERNES"),
            "SATURDAY" to listOf("SATURDAY", "SABADO"),
            "SUNDAY" to listOf("SUNDAY", "DOMINGO")
        )

        val dayOfWeekEn = date.dayOfWeek.name.uppercase()
        val possibleKeys = dayTranslations[dayOfWeekEn] ?: listOf(dayOfWeekEn)
        
        val dayConfigEntry = workingHours.entries.find { entry -> 
            val keyNorm = entry.key.normalize()
            possibleKeys.any { it == keyNorm }
        }

        val config = dayConfigEntry?.value as? Map<*, *>
        val isActive = config?.get("isActive") as? Boolean ?: false
        
        if (dayConfigEntry == null || !isActive) {
            return AvailabilityResponse(specialistId, date, emptyList())
        }

        fun parseFlexTime(timeStr: String?, default: LocalTime): LocalTime {
            if (timeStr.isNullOrBlank()) return default
            return try {
                val clean = timeStr.trim()
                val parts = clean.split(":")
                val h = parts[0].padStart(2, '0').toInt()
                val m = if (parts.size > 1) parts[1].padStart(2, '0').toInt() else 0
                LocalTime.of(h, m)
            } catch (_: Exception) {
                default
            }
        }

        val startTime = parseFlexTime(config["start"]?.toString(), LocalTime.of(9, 0))
        val endTime = parseFlexTime(config["end"]?.toString(), LocalTime.of(18, 0))
        val slotDuration = if (profile.defaultSessionDurationMinutes > 0) profile.defaultSessionDurationMinutes.toLong() else 60L

        val allReservations = reservationRepository.findBySpecialist_IdAndReservationStartBetween(
            actualUserId,
            date.atStartOfDay(),
            date.atTime(LocalTime.MAX)
        ).filter { it.status != ReservationStatus.CANCELLED }

        val availableSlots = mutableListOf<String>()
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        var current = startTime

        while (current.plusMinutes(slotDuration).isBefore(endTime) || current.plusMinutes(slotDuration) == endTime) {
            val slotStart = date.atTime(current)
            val slotEnd = slotStart.plusMinutes(slotDuration)

            val isOccupied = allReservations.any { res ->
                val resEnd = res.reservationEnd ?: res.reservationStart.plusMinutes(60)
                res.reservationStart.isBefore(slotEnd) && resEnd.isAfter(slotStart)
            }

            if (!isOccupied) {
                availableSlots.add(current.format(timeFormatter))
            }
            current = current.plusMinutes(slotDuration)
            if (slotDuration <= 0) break
        }

        return AvailabilityResponse(specialistId, date, availableSlots)
    }

    /**
     * Crea una nueva reservación en el sistema.
     *
     * Realiza validaciones críticas:
     * - Existencia y estado del cliente y especialista.
     * - Pertenencia del servicio al especialista seleccionado.
     * - Detección de traslapes (overlaps) horarios para evitar citas duplicadas.
     *
     * @param request Datos de la reservación a crear.
     * @return [ReservationResponse] con la reservación persistida.
     * @throws IllegalArgumentException si los IDs no existen o el servicio no es válido.
     * @throws IllegalStateException si existe un conflicto de horario.
     */
    @Transactional
    fun create(request: ReservationRequest): ReservationResponse {
        val now = LocalDateTime.now()
        
        // 1. Validar que la reserva no sea en el pasado
        if (request.reservationStart.isBefore(now.plusMinutes(5))) { // Margen de 5 min
            throw IllegalArgumentException("No se pueden realizar reservas para una fecha/hora pasada.")
        }

        val client = userRepository.findById(request.clientId).orElseThrow {
            IllegalArgumentException("El cliente con ID ${request.clientId} no existe.")
        }

        val profile = professionalProfileRepository.findById(request.specialistId)
            .orElseGet { professionalProfileRepository.findByUser_Id(request.specialistId) }
            ?: throw IllegalArgumentException("Perfil profesional no encontrado para el especialista.")

        val specialist = profile.user

        // 2. Validar que el cliente no se reserve a sí mismo
        if (client.id == specialist.id) {
            throw IllegalArgumentException("Un especialista no puede agendar citas consigo mismo.")
        }

        // 3. Validar Perfil Completo del especialista (Regla de Negocio)
        val hasServices = serviceOfferingRepository.findByProfessionalProfile_Id(profile.id!!).isNotEmpty()
        val isProfileComplete = !specialist.rut.isNullOrBlank() && 
                               !specialist.phone.isNullOrBlank() && 
                               hasServices
        
        if (!isProfileComplete) {
            throw IllegalStateException("El especialista seleccionado no tiene su perfil completo y no puede recibir reservas.")
        }

        var service: ServiceOffering? = null
        if (request.serviceId != null) {
            val serviceEntity = serviceOfferingRepository.findById(request.serviceId).orElseThrow {
                IllegalArgumentException("El servicio con ID ${request.serviceId} no existe.")
            }
            if (!serviceEntity.active) {
                throw IllegalArgumentException("El servicio con ID ${request.serviceId} no está activo.")
            }

            if (serviceEntity.professionalProfile.user.id != specialist.id) {
                throw IllegalArgumentException("El servicio seleccionado no pertenece al especialista de la reserva.")
            }
            service = serviceEntity
        }

        val reservationEnd = request.reservationEnd ?: request.reservationStart.plusMinutes(service?.durationMinutes?.toLong() ?: 60L)

        // 4. Validar traslapes ignorando citas canceladas
        val hasConflict = reservationRepository.existsBySpecialist_IdAndReservationStartLessThanAndReservationEndGreaterThanAndStatusNot(
            specialist.id!!,
            reservationEnd,
            request.reservationStart,
            ReservationStatus.CANCELLED
        )

        if (hasConflict) {
            throw IllegalStateException("El especialista ya tiene una cita agendada en este horario.")
        }

        // 5. Validar dirección para servicios a domicilio
        if (service?.isAtHome == true && client.address.isNullOrBlank()) {
            throw IllegalArgumentException("Debes configurar una dirección en tu perfil para solicitar servicios a domicilio.")
        }

        val reservation = Reservation(
            client = client,
            specialist = specialist,
            service = service,
            reservationStart = request.reservationStart,
            reservationEnd = reservationEnd,
            notes = request.notes,
            paymentMethod = request.paymentMethod,
            status = ReservationStatus.PENDING
        )

        val savedReservation = reservationRepository.save(reservation)

        // AUDITORÍA: Registro de creación de cita
        auditLogger.log(
            action = "CREAR_RESERVA",
            targetType = "RESERVATION",
            targetId = savedReservation.id ?: "",
            targetName = "${client.name} con ${specialist.name}",
            details = "Nueva reserva creada para el ${reservation.reservationStart} por un valor de ${service?.price ?: 0}"
        )

        notificationService.createNotification(
            user = client,
            title = "Nueva Cita Agendada",
            message = "Tu cita con ${specialist.name} para el ${reservation.reservationStart} ha sido confirmada.",
            type = com.duoc.app.features.notification.model.NotificationType.CONFIRMATION
        )

        return savedReservation.toResponse()
    }

    /**
     * Obtiene el historial completo de reservaciones de un cliente.
     *
     * @param clientId ID del cliente.
     * @return Lista de [ReservationResponse].
     */
    fun getByClient(clientId: String): List<ReservationResponse> {
        return reservationRepository.findByClient_Id(clientId).map { it.toResponse() }
    }

    /**
     * Obtiene todas las reservaciones asignadas a un especialista.
     *
     * @param specialistId ID del especialista.
     * @return Lista de [ReservationResponse].
     */
    fun getBySpecialist(specialistId: String): List<ReservationResponse> {
        return reservationRepository.findBySpecialist_Id(specialistId).map { it.toResponse() }
    }

    /**
     * Recupera las citas programadas para el día actual para un especialista.
     *
     * @param specialistId ID del especialista.
     * @return Lista de reservaciones filtradas por el rango del día de hoy.
     */
    fun getTodayBySpecialist(specialistId: String): List<ReservationResponse> {
        val startOfDay = LocalDate.now().atStartOfDay()
        val endOfDay = startOfDay.plusDays(1)
        return reservationRepository.findBySpecialist_IdAndReservationStartBetween(specialistId, startOfDay, endOfDay)
            .map { it.toResponse() }
    }

    /**
     * Obtiene las próximas reservaciones (futuras) de un cliente.
     *
     * @param clientId ID del cliente.
     * @return Lista de reservaciones con fecha de inicio posterior a la actual.
     */
    fun getUpcomingByClient(clientId: String): List<ReservationResponse> {
        return reservationRepository.findByClient_IdAndReservationStartAfter(clientId, LocalDateTime.now())
            .map { it.toResponse() }
    }

    /**
     * Actualiza el estado de una reservación y notifica al cliente si es necesario.
     *
     * @param id ID de la reservación.
     * @param status Nuevo estado a aplicar.
     * @return Reservación actualizada.
     */
    @Transactional
    fun updateStatus(id: String, status: ReservationStatus): ReservationResponse {
        val reservation = reservationRepository.findById(id).orElseThrow {
            IllegalArgumentException("Reserva no encontrada con ID: $id")
        }

        reservation.apply {
            this.status = status
            this.updatedAt = LocalDateTime.now()
        }
        val saved = reservationRepository.save(reservation)

        if (status == ReservationStatus.CANCELLED) {
            notificationService.createNotification(
                user = reservation.client,
                title = "Cita Cancelada",
                message = "Tu cita con ${reservation.specialist.name} para el ${reservation.reservationStart} ha sido cancelada.",
                type = com.duoc.app.features.notification.model.NotificationType.ALERT
            )
        }

        return saved.toResponse()
    }

    /**
     * Cancela una reservación existente.
     *
     * @param id ID de la reservación.
     * @param requesterId ID del usuario que solicita la cancelación (Seguridad).
     * @return Reservación con estado [ReservationStatus.CANCELLED].
     */
    @Transactional
    fun cancel(id: String, requesterId: String): ReservationResponse {
        val reservation = reservationRepository.findById(id).orElseThrow {
            IllegalArgumentException("Reserva no encontrada con ID: $id")
        }

        // BLINDAJE: Solo el cliente o el especialista pueden cancelar
        if (reservation.client.id != requesterId && reservation.specialist.id != requesterId) {
            throw IllegalStateException("No tienes permiso para cancelar esta reserva.")
        }

        return updateStatus(id, ReservationStatus.CANCELLED)
    }

    /**
     * Confirma el pago de una reservación y la marca como completada.
     *
     * @param id ID de la reservación.
     * @param requesterId ID del usuario que confirma el pago (Seguridad - Solo especialista).
     * @return Reservación actualizada con el pago confirmado.
     * @throws IllegalStateException si la reserva ya está cancelada o el usuario no tiene permiso.
     */
    @Transactional
    fun confirmPayment(id: String, requesterId: String): ReservationResponse {
        val reservation = reservationRepository.findById(id).orElseThrow {
            IllegalArgumentException("Reserva no encontrada con ID: $id")
        }

        // BLINDAJE: Solo el especialista asignado puede confirmar el pago
        if (reservation.specialist.id != requesterId) {
            throw IllegalStateException("Solo el especialista puede confirmar el pago de esta reserva.")
        }

        if (reservation.status == ReservationStatus.CANCELLED) {
            throw IllegalStateException("No se puede confirmar el pago de una reserva cancelada.")
        }

        // 1. Actualizar estado de la reserva
        reservation.apply {
            status = ReservationStatus.COMPLETED
            updatedAt = LocalDateTime.now()
        }
        val saved = reservationRepository.save(reservation)

        // AUDITORÍA: Registro de pago confirmado
        auditLogger.log(
            action = "CONFIRMAR_PAGO",
            targetType = "RESERVATION",
            targetId = id,
            targetName = "Pago Cita #${id.take(8)}",
            details = "Especialista ${reservation.specialist.name} confirmó pago de ${reservation.client.name}"
        )

        // 2. Gestionar el registro de facturación (BillingRecord)
        // Buscamos si ya existe uno vinculado a esta reserva
        val existingBilling = billingRecordRepository.findByReservation_Id(id).firstOrNull()

        if (existingBilling != null) {
            existingBilling.apply {
                status = com.duoc.app.features.billing.model.PaymentStatus.PAID
                paidAt = LocalDateTime.now()
                paymentMethod = reservation.paymentMethod ?: com.duoc.app.features.billing.model.PaymentMethod.CASH
                updatedAt = LocalDateTime.now()
            }
            billingRecordRepository.save(existingBilling)
        } else {
            // Si no existe, lo creamos como pagado (flujo simplificado para efectivo)
            val servicePrice = reservation.service?.price ?: java.math.BigDecimal.ZERO
            val newBilling = com.duoc.app.features.billing.model.BillingRecord(
                reservation = saved,
                client = reservation.client,
                specialist = reservation.specialist,
                amount = servicePrice,
                status = com.duoc.app.features.billing.model.PaymentStatus.PAID,
                paymentMethod = reservation.paymentMethod ?: com.duoc.app.features.billing.model.PaymentMethod.CASH,
                paidAt = LocalDateTime.now(),
                notes = "Pago confirmado manualmente por el especialista"
            )
            billingRecordRepository.save(newBilling)
        }

        return saved.toResponse()
    }

    /**
     * Tarea programada para limpiar la agenda de citas expiradas.
     * Se ejecuta cada hora.
     * Si una reserva sigue PENDING y ya pasaron más de 2 horas de su inicio, 
     * se marca como EXPIRED para liberar la agenda.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    fun cleanupExpiredReservations() {
        val expirationThreshold = LocalDateTime.now().minusHours(2)
        val expiredReservations = reservationRepository.findByStatusAndReservationStartBefore(
            ReservationStatus.PENDING,
            expirationThreshold
        )

        expiredReservations.forEach { res ->
            res.status = ReservationStatus.CANCELLED
            res.updatedAt = LocalDateTime.now()
            reservationRepository.save(res)
            
            notificationService.createNotification(
                user = res.client,
                title = "Cita Expirada",
                message = "Tu cita con ${res.specialist.name} ha expirado por falta de confirmación.",
                type = com.duoc.app.features.notification.model.NotificationType.ALERT
            )
        }
    }

    /**
     * Conversión de Entidad a DTO.
     * 
     * ¿POR QUÉ ESTO?:
     * Anteriormente, este mapeador realizaba llamadas al professionalProfileRepository 
     * de forma imperativa para cada elemento de una lista (N+1).
     * 
     * AHORA:
     * El servicio es agnóstico a la carga. Confía en que el Repositorio ha inyectado 
     * las relaciones necesarias vía EntityGraph. Si 'this.service' está presente, 
     * sus propiedades anidadas ya están en memoria (Eager Loading controlado).
     * 
     * BENEFICIO: Código más limpio, desacoplado de la persistencia y extremadamente rápido.
     */
    private fun Reservation.toResponse(): ReservationResponse {
        val profile = this.service?.professionalProfile

        return ReservationResponse(
            id = this.id ?: "",
            client = this.client.toSummaryDto(),
            specialist = this.specialist.toSummaryDto(),
            city = profile?.city ?: "",
            address = profile?.address ?: "",
            serviceId = this.service?.id ?: "",
            serviceName = this.service?.name ?: "Servicio no especificado",
            categoryIcon = profile?.category?.iconKey ?: "medical_services",
            categoryColor = profile?.category?.colorHex ?: "#000000",
            isAtHome = this.service?.isAtHome ?: false,
            reservationStart = this.reservationStart,
            reservationEnd = this.reservationEnd,
            status = this.status,
            notes = this.notes ?: "",
            createdAt = this.createdAt
        )
    }
}
