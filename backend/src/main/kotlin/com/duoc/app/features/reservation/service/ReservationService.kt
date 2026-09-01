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
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Motor de Reservas y Disponibilidad.
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

    @Transactional(readOnly = true)
    fun getAvailability(specialistProfileId: String, date: LocalDate): AvailabilityResponse {
        val chileZone = ZoneId.of("America/Santiago")
        val today = LocalDate.now(chileZone)
        val nowTime = LocalTime.now(chileZone)

        // Validar que no se pida disponibilidad para días pasados
        if (date.isBefore(today)) {
            return AvailabilityResponse(specialistProfileId, date, emptyList())
        }

        val profile = professionalProfileRepository.findById(specialistProfileId)
            .orElseGet { professionalProfileRepository.findByUser_Id(specialistProfileId) }
            ?: throw IllegalArgumentException("Perfil profesional no encontrado")

        val profileId = profile.id!!

        val workingHours = try {
            val json = profile.workingHoursJson
            if (!json.isNullOrBlank() && json != "null" && json != "{}") {
                val rawMap: Map<String, Any> = objectMapper.readValue(json, object : TypeReference<Map<String, Any>>() {})
                rawMap.mapKeys { it.key.trim().uppercase() }
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
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
        val isActive = config?.get("isActive")?.toString()?.toBoolean() ?: false
        
        if (dayConfigEntry == null || config == null || !isActive) {
            return AvailabilityResponse(specialistProfileId, date, emptyList())
        }

        fun parseFlexTime(timeStr: String?, default: LocalTime): LocalTime {
            if (timeStr.isNullOrBlank() || timeStr == "null") return default
            return try {
                val clean = timeStr.trim()
                val parts = clean.split(":")
                val h = parts[0].padStart(2, '0').toInt()
                val m = if (parts.size > 1) parts[1].trim().padStart(2, '0').toInt() else 0
                LocalTime.of(h, m)
            } catch (_: Exception) {
                default
            }
        }

        val startTime = parseFlexTime(config["start"]?.toString(), LocalTime.of(9, 0))
        val endTime = parseFlexTime(config["end"]?.toString(), LocalTime.of(18, 0))
        val slotDuration = if (profile.defaultSessionDurationMinutes > 0) profile.defaultSessionDurationMinutes.toLong() else 60L

        val allReservations = try {
            reservationRepository.findBySpecialistIdAndDate(profileId, date)
        } catch (e: Exception) {
            emptyList()
        }

        val availableSlots = mutableListOf<String>()
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        var current = startTime

        while (current.plusMinutes(slotDuration).isBefore(endTime) || current.plusMinutes(slotDuration) == endTime) {
            val slotStart = date.atTime(current)
            val slotEnd = slotStart.plusMinutes(slotDuration)

            // REGLA FLEXIBLE: Solo permitir slots futuros para el día de hoy
            val isPast = (date == today && current.isBefore(nowTime))

            val isOccupied = allReservations.any { res ->
                val resEnd = res.reservationEnd ?: res.reservationStart.plusMinutes(60)
                res.reservationStart.isBefore(slotEnd) && resEnd.isAfter(slotStart)
            }

            if (!isOccupied && !isPast) {
                availableSlots.add(current.format(timeFormatter))
            }
            current = current.plusMinutes(slotDuration)
            if (slotDuration <= 0) break
        }

        return AvailabilityResponse(specialistProfileId, date, availableSlots)
    }

    @Transactional
    fun create(request: ReservationRequest): ReservationResponse {
        val zoneId = ZoneId.of("America/Santiago")
        val now = LocalDateTime.now(zoneId)
        
        if (request.reservationStart.isBefore(now.minusMinutes(1))) {
            throw IllegalArgumentException("No se pueden realizar reservas para una fecha/hora pasada.")
        }

        val client = userRepository.findById(request.clientId).orElseThrow {
            IllegalArgumentException("El cliente no existe.")
        }

        val specialistProfile = professionalProfileRepository.findById(request.specialistProfileId)
            .orElseGet { professionalProfileRepository.findByUser_Id(request.specialistProfileId) }
            ?: throw IllegalArgumentException("Perfil profesional no encontrado.")

        if (client.id == specialistProfile.user.id) {
            throw IllegalArgumentException("Un especialista no puede agendar citas consigo mismo.")
        }

        val hasServices = serviceOfferingRepository.findByProfessionalProfile_Id(specialistProfile.id!!).isNotEmpty()
        val isProfileComplete = !specialistProfile.user.rut.isNullOrBlank() && 
                               !specialistProfile.user.phone.isNullOrBlank() && 
                               hasServices
        
        if (!isProfileComplete) {
            throw IllegalStateException("El especialista no tiene su perfil completo.")
        }

        var service: ServiceOffering? = null
        if (request.serviceId != null) {
            val serviceEntity = serviceOfferingRepository.findById(request.serviceId).orElseThrow {
                IllegalArgumentException("El servicio no existe.")
            }
            if (!serviceEntity.active) {
                throw IllegalArgumentException("El servicio no está activo.")
            }
            if (serviceEntity.professionalProfile.id != specialistProfile.id) {
                throw IllegalArgumentException("El servicio no pertenece al especialista.")
            }
            service = serviceEntity
        }

        val reservationEnd = request.reservationEnd ?: request.reservationStart.plusMinutes(service?.durationMinutes?.toLong() ?: 60L)

        val hasConflict = reservationRepository.existsBySpecialist_IdAndReservationStartLessThanAndReservationEndGreaterThanAndStatusNot(
            specialistProfile.id!!,
            reservationEnd,
            request.reservationStart,
            ReservationStatus.CANCELLED
        )

        if (hasConflict) {
            throw IllegalStateException("El especialista ya tiene una cita agendada en este horario.")
        }

        if (service?.isAtHome == true && client.address.isNullOrBlank()) {
            throw IllegalArgumentException("Debes configurar una dirección en tu perfil.")
        }

        val reservation = Reservation(
            client = client,
            specialist = specialistProfile,
            service = service,
            reservationStart = request.reservationStart,
            reservationEnd = reservationEnd,
            notes = request.notes,
            paymentMethod = request.paymentMethod,
            status = ReservationStatus.PENDING
        )

        val savedReservation = reservationRepository.save(reservation)

        auditLogger.log("CREAR_RESERVA", "RESERVATION", savedReservation.id ?: "", "${client.name} con ${specialistProfile.displayName}", "Nueva reserva")

        notificationService.createNotification(client, "Nueva Cita Agendada", "Tu cita con ${specialistProfile.displayName} ha sido confirmada.", com.duoc.app.features.notification.model.NotificationType.CONFIRMATION)
        notificationService.createNotification(specialistProfile.user, "Nueva Reserva Recibida", "Has recibido una nueva cita de ${client.name}.", com.duoc.app.features.notification.model.NotificationType.CONFIRMATION)

        return savedReservation.toResponse()
    }

    @Transactional(readOnly = true)
    fun getAll(): List<ReservationResponse> = reservationRepository.findAll().map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getByClient(clientId: String): List<ReservationResponse> = reservationRepository.findByClient_IdOrderByCreatedAtDesc(clientId).map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getBySpecialist(specialistProfileId: String): List<ReservationResponse> = reservationRepository.findBySpecialist_IdOrderByCreatedAtDesc(specialistProfileId).map { it.toResponse() }

    @Transactional(readOnly = true)
    fun getTodayBySpecialist(specialistProfileId: String): List<ReservationResponse> {
        val chileZone = ZoneId.of("America/Santiago")
        val startOfDay = LocalDate.now(chileZone).atStartOfDay()
        val endOfDay = startOfDay.plusDays(1)
        return reservationRepository.findBySpecialist_IdAndReservationStartBetween(specialistProfileId, startOfDay, endOfDay).map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getUpcomingByClient(clientId: String): List<ReservationResponse> {
        val chileZone = ZoneId.of("America/Santiago")
        return reservationRepository.findByClient_IdAndReservationStartAfter(clientId, LocalDateTime.now(chileZone)).map { it.toResponse() }
    }

    @Transactional
    fun updateStatus(id: String, status: ReservationStatus): ReservationResponse {
        val reservation = reservationRepository.findById(id).orElseThrow { IllegalArgumentException("Reserva no encontrada") }
        val oldStatus = reservation.status
        reservation.status = status
        reservation.updatedAt = LocalDateTime.now()
        val saved = reservationRepository.save(reservation)
        
        auditLogger.log(
            action = "EDITAR",
            targetType = "Reserva",
            targetId = id,
            targetName = "Reserva #${id.take(8)}",
            details = "Estado cambiado de $oldStatus a $status"
        )
        
        if (status == ReservationStatus.CANCELLED) {
            notificationService.createNotification(reservation.client, "Cita Cancelada", "Tu cita ha sido cancelada.", com.duoc.app.features.notification.model.NotificationType.ALERT)
        }
        return saved.toResponse()
    }

    @Transactional
    fun cancel(id: String, requesterId: String): ReservationResponse {
        val reservation = reservationRepository.findById(id).orElseThrow { IllegalArgumentException("Reserva no encontrada") }
        if (reservation.client.id != requesterId && reservation.specialist.user.id != requesterId) {
            throw IllegalStateException("No tienes permiso")
        }
        val response = updateStatus(id, ReservationStatus.CANCELLED)
        auditLogger.log("ELIMINAR", "Reserva", id, "Reserva #${id.take(8)}", "Cita cancelada")
        return response
    }

    @Transactional
    fun confirmPayment(id: String, requesterId: String): ReservationResponse {
        val chileZone = ZoneId.of("America/Santiago")
        val now = LocalDateTime.now(chileZone)
        
        val reservation = reservationRepository.findById(id).orElseThrow { IllegalArgumentException("Reserva no encontrada") }
        if (reservation.specialist.user.id != requesterId) throw IllegalStateException("No tienes permiso para realizar esta acción.")
        if (reservation.status == ReservationStatus.CANCELLED) throw IllegalStateException("No se puede cobrar una cita que ha sido cancelada.")
        if (reservation.status == ReservationStatus.COMPLETED) throw IllegalStateException("Esta cita ya ha sido pagada y finalizada.")

        // REGLA DE NEGOCIO: Solo se puede cobrar si faltan 60 minutos o menos para el inicio, o si ya pasó.
        val limitForEarlyPayment = reservation.reservationStart.minusMinutes(60)
        if (now.isBefore(limitForEarlyPayment)) {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            throw IllegalStateException("La atención no puede finalizarse todavía. Por seguridad y para garantizar la integridad del registro, el cierre de la cita se habilitará 60 minutos antes de la hora acordada (disponible desde las ${limitForEarlyPayment.format(formatter)} hrs).")
        }

        reservation.status = ReservationStatus.COMPLETED
        reservation.updatedAt = now
        val saved = reservationRepository.save(reservation)

        auditLogger.log("CONFIRMAR_PAGO", "RESERVATION", id, "Pago Cita #${id.take(8)}", "Confirmación de pago")

        val existingBilling = billingRecordRepository.findByReservation_Id(id).firstOrNull()
        if (existingBilling != null) {
            existingBilling.status = com.duoc.app.features.billing.model.PaymentStatus.PAID
            existingBilling.paidAt = LocalDateTime.now()
            billingRecordRepository.save(existingBilling)
        } else {
            billingRecordRepository.save(com.duoc.app.features.billing.model.PaymentStatus.PAID.let { 
                com.duoc.app.features.billing.model.BillingRecord(
                    reservation = saved,
                    amount = reservation.service?.price ?: java.math.BigDecimal.ZERO,
                    status = it,
                    paymentMethod = reservation.paymentMethod ?: com.duoc.app.features.billing.model.PaymentMethod.CASH,
                    paidAt = LocalDateTime.now()
                )
            })
        }
        return saved.toResponse()
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    fun cleanupExpiredReservations() {
        val expirationThreshold = LocalDateTime.now().minusHours(2)
        reservationRepository.findByStatusAndReservationStartBefore(ReservationStatus.PENDING, expirationThreshold).forEach { res ->
            res.status = ReservationStatus.CANCELLED
            res.updatedAt = LocalDateTime.now()
            reservationRepository.save(res)
            
            auditLogger.log(
                action = "EXPIRAR",
                targetType = "Reserva",
                targetId = res.id ?: "",
                targetName = "Reserva #${res.id?.take(8)}",
                details = "Cita expirada automáticamente por falta de confirmación/pago"
            )

            notificationService.createNotification(res.client, "Cita Expirada", "Tu cita ha expirado.", com.duoc.app.features.notification.model.NotificationType.ALERT)
        }
    }

    private fun Reservation.toResponse(): ReservationResponse {
        val specialistProfile = this.specialist
        return ReservationResponse(
            id = this.id ?: "",
            client = this.client.toSummaryDto(),
            specialist = this.specialist.user.toSummaryDto(),
            specialistProfileId = specialistProfile.id ?: "",
            city = specialistProfile.city ?: "",
            address = specialistProfile.address ?: "",
            serviceId = this.service?.id ?: "",
            serviceName = this.service?.name ?: "No especificado",
            categoryIcon = specialistProfile.category?.iconKey ?: "medical_services",
            categoryColor = specialistProfile.category?.colorHex ?: "#000000",
            isAtHome = this.service?.isAtHome ?: false,
            reservationStart = this.reservationStart,
            reservationEnd = this.reservationEnd,
            status = this.status,
            notes = this.notes ?: "",
            createdAt = this.createdAt
        )
    }
}
