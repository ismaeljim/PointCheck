package com.duoc.app.features.dashboard.service

import com.duoc.app.features.attention.repository.AttentionRepository
import com.duoc.app.features.billing.model.PaymentStatus
import com.duoc.app.features.billing.repository.BillingRecordRepository
import com.duoc.app.features.dashboard.dto.DashboardMetricsResponse
import com.duoc.app.features.professionalprofile.repository.ProfessionalProfileRepository
import com.duoc.app.features.reservation.repository.ReservationRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

import com.duoc.app.features.dashboard.dto.ReportSummaryResponse

@Service
class DashboardService(
    private val reservationRepository: ReservationRepository,
    private val attentionRepository: AttentionRepository,
    private val billingRecordRepository: BillingRecordRepository,
    private val profileRepository: ProfessionalProfileRepository,
    private val notificationService: com.duoc.app.features.notification.service.NotificationService
) {

    fun getReportSummary(userId: Long): ReportSummaryResponse {
        val profile = profileRepository.findByUser_Id(userId) ?: return ReportSummaryResponse()
        val specialistId = profile.id

        val reservations = reservationRepository.findBySpecialist_Id(specialistId)
        val today = LocalDate.now()
        val todayStart = LocalDateTime.of(today, LocalTime.MIN)
        val todayEnd = LocalDateTime.of(today, LocalTime.MAX)
        
        val todayReservations = reservationRepository.findBySpecialist_IdAndReservationStartBetween(
            specialistId, todayStart, todayEnd
        )
        
        val attentions = attentionRepository.findBySpecialist_Id(specialistId)
        val billing = billingRecordRepository.findBySpecialist_Id(specialistId)

        return ReportSummaryResponse(
            totalReservations = reservations.size,
            todayReservations = todayReservations.size,
            completedAttentions = attentions.size,
            averageAttentionMinutes = if (attentions.isNotEmpty()) {
                attentions.mapNotNull { it.durationMinutes }.average().let { if (it.isNaN()) 0.0 else it }
            } else 0.0,
            totalCharged = billing.filter { it.status == PaymentStatus.PAID }.sumOf { it.amount.toDouble() },
            pendingAmount = billing.filter { it.status == PaymentStatus.PENDING }.sumOf { it.amount.toDouble() },
            paidBillingCount = billing.count { it.status == PaymentStatus.PAID },
            pendingBillingCount = billing.count { it.status == PaymentStatus.PENDING }
        )
    }

    fun getMetrics(userId: Long, role: String): DashboardMetricsResponse {
        val now = LocalDateTime.now()
        
        return if (role.equals("CLIENT", ignoreCase = true)) {
            val upcoming = reservationRepository.findByClient_IdAndReservationStartAfter(userId, now)
            val all = reservationRepository.findByClient_Id(userId)
            
            DashboardMetricsResponse(
                upcomingReservationsCount = upcoming.size,
                recentReservationsCount = all.size,
                lastReservationStatus = all.maxByOrNull { it.updatedAt!! }?.status?.name
            )
        } else {
            val profile = profileRepository.findByUser_Id(userId)
            if (profile == null) return DashboardMetricsResponse()

            val todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN)
            val todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX)
            
            val todayReservations = reservationRepository.findBySpecialist_IdAndReservationStartBetween(
                profile.id, todayStart, todayEnd
            )
            
            val attentions = attentionRepository.findBySpecialist_Id(profile.id)
            val pendingBilling = billingRecordRepository.findBySpecialist_IdAndStatus(profile.id, PaymentStatus.PENDING)
            val paidBilling = billingRecordRepository.findBySpecialist_IdAndStatus(profile.id, PaymentStatus.PAID)

            DashboardMetricsResponse(
                appointmentsToday = todayReservations.size,
                totalAttentionsPerformed = attentions.size,
                averageDurationMinutes = if (attentions.isNotEmpty()) {
                    val durations = attentions.mapNotNull { it.durationMinutes }
                    if (durations.isNotEmpty()) durations.average() else 0.0
                } else 0.0,
                pendingBillingAmount = pendingBilling.sumOf { it.amount.toDouble() },
                paidBillingAmount = paidBilling.sumOf { it.amount.toDouble() },
                subscriptionStatus = "ACTIVE" // Placeholder
            )
        }
    }

    fun getClientDashboard(userId: Long): com.duoc.app.features.dashboard.dto.ClientDashboardResponse {
        val now = LocalDateTime.now()
        
        // 1. Próxima cita
        val nextAppointment = reservationRepository.findByClient_IdAndReservationStartAfter(userId, now)
            .filter { it.status != com.duoc.app.features.reservation.model.ReservationStatus.CANCELLED }
            .minByOrNull { it.reservationStart }
            ?.toResponse()

        // 2. Especialistas favoritos (basado en conteo de citas)
        val favoriteSpecialists = reservationRepository.findByClient_Id(userId)
            .groupBy { it.specialist }
            .map { (specialist, reservations) ->
                val profile = profileRepository.findByUser_Id(specialist.id)
                com.duoc.app.features.dashboard.dto.FavoriteSpecialistDto(
                    specialistId = specialist.id,
                    name = specialist.name,
                    specialty = profile?.specialty,
                    visitCount = reservations.size.toLong()
                )
            }
            .sortedByDescending { it.visitCount }
            .take(3)

        // 3. Notificaciones recientes
        val recentNotifications = notificationService.getRecentNotifications(userId, 3)
            .map { 
                com.duoc.app.features.dashboard.dto.NotificationSummaryDto(
                    id = it.id,
                    title = it.title,
                    message = it.message,
                    type = it.type.name,
                    isRead = it.isRead,
                    createdAt = it.createdAt
                )
            }

        return com.duoc.app.features.dashboard.dto.ClientDashboardResponse(
            nextAppointment = nextAppointment,
            favoriteSpecialists = favoriteSpecialists,
            recentNotifications = recentNotifications
        )
    }

    // Helper extension to convert Reservation to ReservationResponse if not already available in the scope
    private fun com.duoc.app.features.reservation.model.Reservation.toResponse() = 
        com.duoc.app.features.reservation.dto.ReservationResponse(
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
