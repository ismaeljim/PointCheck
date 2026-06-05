package com.duoc.app.features.dashboard.service

import com.duoc.app.features.attention.repository.AttentionRepository
import com.duoc.app.features.billing.model.PaymentStatus
import com.duoc.app.features.billing.repository.BillingRecordRepository
import com.duoc.app.features.dashboard.dto.DashboardMetricsResponse
import com.duoc.app.features.professionalprofile.repository.ProfessionalProfileRepository
import com.duoc.app.features.reservation.repository.ReservationRepository
import com.duoc.app.features.reservation.model.ReservationStatus
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.math.BigDecimal

import com.duoc.app.features.dashboard.dto.ReportSummaryResponse

@Service
class DashboardService(
    private val reservationRepository: ReservationRepository,
    private val attentionRepository: AttentionRepository,
    private val billingRecordRepository: BillingRecordRepository,
    private val profileRepository: ProfessionalProfileRepository,
    private val userRepository: com.duoc.app.features.user.repository.UserRepository,
    private val notificationService: com.duoc.app.features.notification.service.NotificationService
) {

    fun getReportSummary(userId: String): com.duoc.app.features.report.dto.ReportSummaryResponse {
        val now = LocalDateTime.now()
        val todayStart = now.toLocalDate().atStartOfDay()
        val todayEnd = now.toLocalDate().atTime(LocalTime.MAX)
        val monthStart = now.withDayOfMonth(1).toLocalDate().atStartOfDay()

        val profile = profileRepository.findByUser_Id(userId)
        
        // Optimizamos usando conteos directos en DB en lugar de traer listas completas
        val totalReservations = reservationRepository.countBySpecialist_Id(userId)
        val todayReservations = reservationRepository.countBySpecialist_IdAndReservationStartBetween(userId, todayStart, todayEnd)
        
        val completedAttentionsCount = attentionRepository.countBySpecialist_IdAndStartedAtBetweenAndStatus(
            userId, monthStart, todayEnd, com.duoc.app.features.attention.model.AttentionStatus.FINISHED
        )
        val completedReservationsCount = reservationRepository.countBySpecialist_IdAndReservationStartBetweenAndStatus(
            userId, monthStart, todayEnd, ReservationStatus.COMPLETED
        )

        val totalDurationAttentions = attentionRepository.sumDurationMinutesBySpecialistAndStartedAtBetweenAndStatus(
            userId, monthStart, todayEnd, com.duoc.app.features.attention.model.AttentionStatus.FINISHED
        ) ?: 0L
        val totalDurationReservations = reservationRepository.sumServiceDurationMinutesBySpecialistAndReservationStartBetweenAndStatus(
            userId, monthStart, todayEnd, com.duoc.app.features.reservation.model.ReservationStatus.COMPLETED
        ) ?: 0L

        val totalPerformed = (completedAttentionsCount + completedReservationsCount).toInt()

        val totalCharged = billingRecordRepository.sumAmountBySpecialistAndCreatedAtBetweenAndStatus(
            userId, monthStart, todayEnd, PaymentStatus.PAID
        ) ?: BigDecimal.ZERO

        val pendingAmount = billingRecordRepository.sumAmountBySpecialistAndCreatedAtBetweenAndStatus(
            userId, monthStart, todayEnd, PaymentStatus.PENDING
        ) ?: BigDecimal.ZERO

        val paidBillingCount = billingRecordRepository.countBySpecialist_IdAndCreatedAtBetweenAndStatus(
            userId, monthStart, todayEnd, PaymentStatus.PAID
        )

        val pendingBillingCount = billingRecordRepository.countBySpecialist_IdAndCreatedAtBetweenAndStatus(
            userId, monthStart, todayEnd, PaymentStatus.PENDING
        )

        return com.duoc.app.features.report.dto.ReportSummaryResponse(
            totalReservations = totalReservations.toInt(),
            todayReservations = todayReservations.toInt(),
            completedAttentions = totalPerformed,
            averageAttentionMinutes = if (totalPerformed > 0) (totalDurationAttentions + totalDurationReservations).toDouble() / totalPerformed else 0.0,
            totalCharged = totalCharged,
            pendingAmount = pendingAmount,
            paidBillingCount = paidBillingCount.toInt(),
            pendingBillingCount = pendingBillingCount.toInt(),
            specialty = profile?.specialty
        )
    }

    fun getMetrics(userId: String, role: String): DashboardMetricsResponse {
        val now = LocalDateTime.now()
        
        return when {
            role.equals("ADMIN", ignoreCase = true) -> {
                val totalReservations = reservationRepository.count()
                val totalClients = userRepository.countByRole(com.duoc.app.features.user.model.UserRole.CLIENT)
                val totalSpecialists = userRepository.countByRole(com.duoc.app.features.user.model.UserRole.SPECIALIST)
                val totalRevenue = billingRecordRepository.findByStatus(PaymentStatus.PAID).sumOf { it.amount.toDouble() }

                DashboardMetricsResponse(
                    appointmentsToday = totalReservations.toInt(),
                    totalAttentionsPerformed = totalClients.toInt(),
                    averageDurationMinutes = totalSpecialists.toDouble(),
                    paidBillingAmount = totalRevenue,
                    subscriptionStatus = "ADMIN_MODE"
                )
            }
            role.equals("CLIENT", ignoreCase = true) -> {
                val upcoming = reservationRepository.findByClient_IdAndReservationStartAfter(userId, now)
                val all = reservationRepository.findByClient_Id(userId)
                
                DashboardMetricsResponse(
                    upcomingReservationsCount = upcoming.size,
                    recentReservationsCount = all.size,
                    lastReservationStatus = all.maxByOrNull { it.updatedAt ?: it.createdAt }?.status?.name
                )
            }
            else -> {
                val profile = profileRepository.findByUser_Id(userId)
                if (profile == null) return DashboardMetricsResponse()

                val now = LocalDateTime.now()
                val todayStart = now.toLocalDate().atStartOfDay()
                val todayEnd = now.toLocalDate().atTime(LocalTime.MAX)
                val monthStart = now.withDayOfMonth(1).toLocalDate().atStartOfDay()
                val monthEnd = todayEnd // Hasta el final del día de hoy

                // 1. Métricas de Reservas (Hoy y Mes) - CONSULTAS DE CONTEO DIRECTO
                val appointmentsToday = reservationRepository.countBySpecialist_IdAndReservationStartBetween(
                    userId, todayStart, todayEnd
                ).toInt()
                
                val appointmentsMonth = reservationRepository.countBySpecialist_IdAndReservationStartBetween(
                    userId, monthStart, todayEnd.plusDays(30)
                ).toInt()

                // 2. Métricas de Atención y Duración (SQL SUM/AVG)
                val completedAttentionsCount = attentionRepository.countBySpecialist_IdAndStartedAtBetweenAndStatus(
                    userId, monthStart, todayEnd, com.duoc.app.features.attention.model.AttentionStatus.FINISHED
                )
                
                val completedReservationsCount = reservationRepository.countBySpecialist_IdAndReservationStartBetweenAndStatus(
                    userId, monthStart, todayEnd, ReservationStatus.COMPLETED
                )

                val totalDurationAttentions = attentionRepository.sumDurationMinutesBySpecialistAndStartedAtBetweenAndStatus(
                    userId, monthStart, todayEnd, com.duoc.app.features.attention.model.AttentionStatus.FINISHED
                ) ?: 0L
                
                val totalDurationReservations = reservationRepository.sumServiceDurationMinutesBySpecialistAndReservationStartBetweenAndStatus(
                    userId, monthStart, todayEnd, com.duoc.app.features.reservation.model.ReservationStatus.COMPLETED
                ) ?: 0L

                val totalPerformed = (completedAttentionsCount + completedReservationsCount).toInt()
                val avgDuration = if (totalPerformed > 0) {
                    (totalDurationAttentions + totalDurationReservations).toDouble() / totalPerformed
                } else 0.0

                // 3. Métricas Financieras (SQL SUM directa)
                val pendingAmount = billingRecordRepository.sumAmountBySpecialistAndCreatedAtBetweenAndStatus(
                    userId, monthStart, todayEnd, PaymentStatus.PENDING
                )?.toDouble() ?: 0.0

                val paidAmount = billingRecordRepository.sumAmountBySpecialistAndCreatedAtBetweenAndStatus(
                    userId, monthStart, todayEnd, PaymentStatus.PAID
                )?.toDouble() ?: 0.0

                DashboardMetricsResponse(
                    appointmentsToday = appointmentsToday,
                    appointmentsMonth = appointmentsMonth,
                    totalAttentionsPerformed = totalPerformed,
                    averageDurationMinutes = avgDuration,
                    pendingBillingAmount = pendingAmount,
                    paidBillingAmount = paidAmount,
                    subscriptionStatus = "ACTIVE",
                    specialty = profile.specialty
                )
            }
        }
    }

    fun getClientDashboard(userId: String): com.duoc.app.features.dashboard.dto.ClientDashboardResponse {
        val now = LocalDateTime.now()
        
        val nextAppointment = reservationRepository.findByClient_IdAndReservationStartAfter(userId, now)
            .filter { it.status != com.duoc.app.features.reservation.model.ReservationStatus.CANCELLED }
            .minByOrNull { it.reservationStart }
            ?.toResponse()

        val favoriteSpecialists = reservationRepository.findByClient_Id(userId)
            .groupBy { it.specialist }
            .map { (specialist, reservations) ->
                val profile = profileRepository.findByUser_Id(specialist.id!!)
                com.duoc.app.features.dashboard.dto.FavoriteSpecialistDto(
                    specialistId = profile?.id ?: specialist.id!!,
                    name = specialist.name,
                    specialty = profile?.specialty,
                    visitCount = reservations.size.toLong()
                )
            }
            .sortedByDescending { it.visitCount }
            .take(3)

        val recentNotifications = notificationService.getRecentNotifications(userId, 3)
            .map { 
                com.duoc.app.features.dashboard.dto.NotificationSummaryDto(
                    id = it.id!!,
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

    private fun com.duoc.app.features.reservation.model.Reservation.toResponse(): com.duoc.app.features.reservation.dto.ReservationResponse {
        val profProfile = this.service?.professionalProfile
        return com.duoc.app.features.reservation.dto.ReservationResponse(
            id = this.id!!,
            client = this.client.toSummaryDto(),
            specialist = this.specialist.toSummaryDto(),
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
