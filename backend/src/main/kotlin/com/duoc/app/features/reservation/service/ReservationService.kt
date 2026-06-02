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
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * AUDITORÍA TÉCNICA: Motor de Reservas y Disponibilidad
 * 
 * Este componente es el núcleo del negocio. Gestiona la intersección entre 
 * la agenda del especialista y las necesidades del cliente.
 * 
 * Hallazgos de Implementación:
 * 1. [CRÍTICO] Lógica de Disponibilidad: El método 'getAvailability' realiza un parseo flexible de JSON 
 *    para manejar los horarios de trabajo. Es resiliente pero costoso computacionalmente (parseo por cada request).
 * 2. [OK] Prevención de Overlapping: Implementada validación de traslape mediante 'existsBySpecialist_IdAndReservationStartLessThanAndReservationEndGreaterThan'.
 * 3. [MEJORA] Atomicidad: Se recomienda @Transactional para el proceso de creación y envío de notificaciones.
 * 4. [OK] Integración de Notificaciones: El flujo de reserva gatilla alertas automáticas.
 */
@Service
class ReservationService(
    private val reservationRepository: ReservationRepository,
    private val userRepository: UserRepository,
    private val serviceOfferingRepository: ServiceOfferingRepository,
    private val professionalProfileRepository: ProfessionalProfileRepository,
    private val notificationService: com.duoc.app.features.notification.service.NotificationService
) {

    private val objectMapper = jacksonObjectMapper()

    /**
     * AUDITORÍA: Algoritmo de generación de slots temporales.
     * Soporta internacionalización básica y normalización de caracteres para las llaves del JSON (Lunes vs Monday).
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

        if (dayConfigEntry == null) {
            return AvailabilityResponse(specialistId, date, emptyList())
        }

        val config = dayConfigEntry.value
        val (startStr, endStr) = when (config) {
            is Map<*, *> -> Pair(config["start"]?.toString(), config["end"]?.toString())
            else -> Pair("09:00", "18:00")
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

        val startTime = parseFlexTime(startStr, LocalTime.of(9, 0))
        val endTime = parseFlexTime(endStr, LocalTime.of(18, 0))
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
                res.reservationStart.isBefore(slotEnd) && (res.reservationEnd?.isAfter(slotStart) ?: true)
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
     * AUDITORÍA: Creación de reserva.
     * 1. Valida existencia de cliente.
     * 2. Valida pertenencia del servicio al especialista.
     * 3. Verifica conflictos de horario en tiempo real.
     * 4. Gatilla notificación de confirmación asíncrona (conceptual).
     */
    fun create(request: ReservationRequest): ReservationResponse {
        val client = userRepository.findById(request.clientId).orElseThrow {
            IllegalArgumentException("El cliente con ID ${request.clientId} no existe.")
        }

        val profile = professionalProfileRepository.findById(request.specialistId)
            .orElseGet { professionalProfileRepository.findByUser_Id(request.specialistId) }
            ?: throw IllegalArgumentException("Perfil profesional no encontrado para el especialista.")

        val specialist = profile.user

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

        val reservationEnd = request.reservationEnd ?: request.reservationStart.plusMinutes(60)
        val hasConflict = reservationRepository.existsBySpecialist_IdAndReservationStartLessThanAndReservationEndGreaterThan(
            specialist.id!!,
            reservationEnd,
            request.reservationStart
        )

        if (hasConflict) {
            throw IllegalStateException("El especialista ya tiene una cita agendada en este horario.")
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

        notificationService.createNotification(
            user = client,
            title = "Nueva Cita Agendada",
            message = "Tu cita con ${specialist.name} para el ${reservation.reservationStart} ha sido confirmada.",
            type = com.duoc.app.features.notification.model.NotificationType.CONFIRMATION
        )

        return savedReservation.toResponse()
    }

    fun getByClient(clientId: String): List<ReservationResponse> {
        return reservationRepository.findByClient_Id(clientId).map { it.toResponse() }
    }

    fun getBySpecialist(specialistId: String): List<ReservationResponse> {
        return reservationRepository.findBySpecialist_Id(specialistId).map { it.toResponse() }
    }

    fun getTodayBySpecialist(specialistId: String): List<ReservationResponse> {
        val startOfDay = LocalDate.now().atStartOfDay()
        val endOfDay = startOfDay.plusDays(1)
        return reservationRepository.findBySpecialist_IdAndReservationStartBetween(specialistId, startOfDay, endOfDay)
            .map { it.toResponse() }
    }

    fun getUpcomingByClient(clientId: String): List<ReservationResponse> {
        return reservationRepository.findByClient_IdAndReservationStartAfter(clientId, LocalDateTime.now())
            .map { it.toResponse() }
    }

    fun updateStatus(id: String, status: ReservationStatus): ReservationResponse {
        val reservation = reservationRepository.findById(id).orElseThrow {
            IllegalArgumentException("Reserva no encontrada con ID: $id")
        }

        val updatedReservation = reservation.copy(
            status = status,
            updatedAt = LocalDateTime.now()
        )
        val saved = reservationRepository.save(updatedReservation)

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

    fun cancel(id: String): ReservationResponse {
        return updateStatus(id, ReservationStatus.CANCELLED)
    }

    private fun Reservation.toResponse(): ReservationResponse {
        val profProfile = this.service?.professionalProfile ?: professionalProfileRepository.findByUser_Id(this.specialist.id!!)

        return ReservationResponse(
            id = this.id!!,
            clientId = this.client.id!!,
            clientRut = this.client.rut,
            specialistId = this.specialist.id!!,
            specialistName = this.specialist.name,
            specialistRut = this.specialist.rut,
            city = profProfile?.city,
            address = profProfile?.address,
            serviceId = this.service?.id,
            serviceName = this.service?.name,
            categoryIcon = profProfile?.category?.iconKey,
            categoryColor = profProfile?.category?.colorHex,
            isAtHome = this.service?.isAtHome ?: false,
            reservationStart = this.reservationStart,
            reservationEnd = this.reservationEnd,
            status = this.status,
            notes = this.notes,
            createdAt = this.createdAt
        )
    }
}
