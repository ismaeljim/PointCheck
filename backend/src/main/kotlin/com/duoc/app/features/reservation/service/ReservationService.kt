package com.duoc.app.features.reservation.service

import com.duoc.app.features.professionalprofile.repository.ProfessionalProfileRepository
import com.duoc.app.features.reservation.dto.ReservationRequest
import com.duoc.app.features.reservation.dto.ReservationResponse
import com.duoc.app.features.reservation.model.Reservation
import com.duoc.app.features.reservation.model.ReservationStatus
import com.duoc.app.features.reservation.repository.ReservationRepository
import com.duoc.app.features.service.model.ServiceOffering
import com.duoc.app.features.service.repository.ServiceOfferingRepository
import com.duoc.app.features.user.model.UserRole
import com.duoc.app.features.user.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class ReservationService(
    private val reservationRepository: ReservationRepository,
    private val userRepository: UserRepository,
    private val serviceOfferingRepository: ServiceOfferingRepository,
    private val professionalProfileRepository: ProfessionalProfileRepository,
    private val notificationService: com.duoc.app.features.notification.service.NotificationService
) {

    fun create(request: ReservationRequest): ReservationResponse {
        val client = userRepository.findById(request.clientId).orElseThrow {
            IllegalArgumentException("El cliente con ID ${request.clientId} no existe.")
        }
        // Permitimos que cualquier usuario (CLIENT o SPECIALIST) pueda agendar una cita

        val specialist = userRepository.findById(request.specialistId).orElseThrow {
            IllegalArgumentException("El especialista con ID ${request.specialistId} no existe.")
        }
        if (specialist.role != UserRole.SPECIALIST) {
            throw IllegalArgumentException("El usuario con ID ${request.specialistId} no es un especialista.")
        }

        var service: ServiceOffering? = null
        if (request.serviceId != null) {
            val serviceEntity = serviceOfferingRepository.findById(request.serviceId).orElseThrow {
                IllegalArgumentException("El servicio con ID ${request.serviceId} no existe.")
            }
            if (!serviceEntity.active) {
                throw IllegalArgumentException("El servicio con ID ${request.serviceId} no está activo.")
            }

            // Validar que el servicio pertenece al professional profile cuyo userId corresponde al specialistId de la reserva
            val profile = professionalProfileRepository.findById(serviceEntity.professionalProfile.id).orElseThrow {
                IllegalArgumentException("Perfil profesional no encontrado para el servicio.")
            }
            if (profile.user.id != request.specialistId) {
                throw IllegalArgumentException("El servicio seleccionado no pertenece al especialista de la reserva.")
            }
            service = serviceEntity
        }

        // Validación de Traslape de Horarios (Conflictos)
        val reservationEnd = request.reservationEnd ?: request.reservationStart.plusMinutes(60)
        val hasConflict = reservationRepository.existsBySpecialist_IdAndReservationStartLessThanEqualAndReservationEndGreaterThanEqual(
            request.specialistId,
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
            status = ReservationStatus.PENDING
        )

        val savedReservation = reservationRepository.save(reservation)

        // Paso 2: Notificar al cliente sobre la nueva reserva
        notificationService.createNotification(
            user = client,
            title = "Nueva Cita Agendada",
            message = "Tu cita con ${specialist.name} para el ${reservation.reservationStart} ha sido confirmada.",
            type = com.duoc.app.features.notification.model.NotificationType.CONFIRMATION
        )

        return savedReservation.toResponse()
    }

    fun getByClient(clientId: Long): List<ReservationResponse> {
        return reservationRepository.findByClient_Id(clientId).map { it.toResponse() }
    }

    fun getBySpecialist(specialistId: Long): List<ReservationResponse> {
        return reservationRepository.findBySpecialist_Id(specialistId).map { it.toResponse() }
    }

    fun getTodayBySpecialist(specialistId: Long): List<ReservationResponse> {
        val startOfDay = LocalDate.now().atStartOfDay()
        val endOfDay = startOfDay.plusDays(1)
        return reservationRepository.findBySpecialist_IdAndReservationStartBetween(specialistId, startOfDay, endOfDay)
            .map { it.toResponse() }
    }

    fun getUpcomingByClient(clientId: Long): List<ReservationResponse> {
        return reservationRepository.findByClient_IdAndReservationStartAfter(clientId, LocalDateTime.now())
            .map { it.toResponse() }
    }

    fun updateStatus(id: Long, status: ReservationStatus): ReservationResponse {
        val reservation = reservationRepository.findById(id).orElseThrow {
            IllegalArgumentException("Reserva no encontrada con ID: $id")
        }

        val updatedReservation = reservation.copy(
            status = status,
            updatedAt = java.time.LocalDateTime.now()
        )
        val saved = reservationRepository.save(updatedReservation)

        // Paso 2: Notificar si se cancela
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

    fun cancel(id: Long): ReservationResponse {
        return updateStatus(id, ReservationStatus.CANCELLED)
    }

    private fun Reservation.toResponse(): ReservationResponse = ReservationResponse(
        id = this.id,
        clientId = this.client.id,
        specialistId = this.specialist.id,
        specialistName = this.specialist.name,
        city = this.service?.professionalProfile?.city,
        address = this.service?.professionalProfile?.address,
        serviceId = this.service?.id,
        serviceName = this.service?.name,
        reservationStart = this.reservationStart,
        reservationEnd = this.reservationEnd,
        status = this.status,
        notes = this.notes,
        createdAt = this.createdAt
    )
}
