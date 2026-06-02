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
        val profile = profileRepository.findByUser_Id(userId)
        val allReservations = reservationRepository.findBySpecialist_Id(userId)
        val today = LocalDate.now()
        
        val todayReservations = allReservations.filter { 
            it.reservationStart.toLocalDate().isEqual(today)
        }
        
        val attentions = attentionRepository.findBySpecialist_Id(userId)
        val completedReservations = allReservations.filter { it.status == com.duoc.app.features.reservation.model.ReservationStatus.COMPLETED }
        
        val allDurations = mutableListOf<Double>()
        attentions.forEach { it.durationMinutes?.let { d -> allDurations.add(d.toDouble()) } }
        completedReservations.forEach { res ->
            res.service?.durationMinutes?.let { d -> allDurations.add(d.toDouble()) }
        }

        val billing = billingRecordRepository.findBySpecialist_Id(userId)

        return com.duoc.app.features.report.dto.ReportSummaryResponse(
            totalReservations = allReservations.size,
            todayReservations = todayReservations.size,
            completedAttentions = attentions.size + completedReservations.size,
            averageAttentionMinutes = if (allDurations.isNotEmpty()) allDurations.average() else 0.0,
            totalCharged = billing.filter { it.status == PaymentStatus.PAID }.map { it.amount }
                .fold(BigDecimal.ZERO, BigDecimal::add),
            pendingAmount = billing.filter { it.status == PaymentStatus.PENDING }.map { it.amount }
                .fold(BigDecimal.ZERO, BigDecimal::add),
            paidBillingCount = billing.count { it.status == PaymentStatus.PAID },
            pendingBillingCount = billing.count { it.status == PaymentStatus.PENDING },
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

                val todayReservations = reservationRepository.findBySpecialist_IdAndReservationStartBetween(
                    userId, todayStart, todayEnd
                )
                
                val monthReservations = reservationRepository.findBySpecialist_IdAndReservationStartBetween(
                    userId, monthStart, todayEnd.plusDays(30) // Just to be safe for the month
                ).filter { it.reservationStart.month == now.month && it.reservationStart.year == now.year }

                val attentions = attentionRepository.findBySpecialist_Id(userId)
                val completedReservations = monthReservations.filter { it.status == com.duoc.app.features.reservation.model.ReservationStatus.COMPLETED }
                
                val allDurations = mutableListOf<Double>()
                attentions.forEach { it.durationMinutes?.let { d -> allDurations.add(d.toDouble()) } }
                completedReservations.forEach { res ->
                    res.service?.durationMinutes?.let { d -> allDurations.add(d.toDouble()) }
                }

                val billing = billingRecordRepository.findBySpecialist_Id(userId)

                val monthlyBilling = billing.filter { 
                    it.createdAt.month == now.month && it.createdAt.year == now.year
                }

                DashboardMetricsResponse(
                    appointmentsToday = todayReservations.size,
                    appointmentsMonth = monthReservations.size,
                    totalAttentionsPerformed = attentions.size + completedReservations.size,
                    averageDurationMinutes = if (allDurations.isNotEmpty()) allDurations.average() else 0.0,
                    pendingBillingAmount = monthlyBilling.filter { it.status == PaymentStatus.PENDING }.sumOf { it.amount.toDouble() },
                    paidBillingAmount = monthlyBilling.filter { it.status == PaymentStatus.PAID }.sumOf { it.amount.toDouble() },
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
