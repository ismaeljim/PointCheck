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

@Service
class DashboardService(
    private val reservationRepository: ReservationRepository,
    private val attentionRepository: AttentionRepository,
    private val billingRecordRepository: BillingRecordRepository,
    private val profileRepository: ProfessionalProfileRepository
) {

    fun getMetrics(userId: Long, role: String): DashboardMetricsResponse {
        val now = LocalDateTime.now()
        
        return if (role.equals("CLIENT", ignoreCase = true)) {
            val upcoming = reservationRepository.findByClient_IdAndReservationStartAfter(userId, now)
            val all = reservationRepository.findByClient_Id(userId)
            
            DashboardMetricsResponse(
                upcomingReservationsCount = upcoming.size,
                recentReservationsCount = all.size,
                lastReservationStatus = all.maxByOrNull { it.updatedAt }?.status?.name
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
                averageDurationMinutes = if (attentions.isNotEmpty()) attentions.map { it.durationMinutes }.average() else 0.0,
                pendingBillingAmount = pendingBilling.sumOf { it.amount.toDouble() },
                paidBillingAmount = paidBilling.sumOf { it.amount.toDouble() },
                subscriptionStatus = "ACTIVE" // Placeholder
            )
        }
    }
}
