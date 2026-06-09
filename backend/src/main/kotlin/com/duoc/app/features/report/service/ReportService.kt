package com.duoc.app.features.report.service

import com.duoc.app.features.attention.model.AttentionStatus
import com.duoc.app.features.attention.repository.AttentionRepository
import com.duoc.app.features.billing.model.PaymentStatus
import com.duoc.app.features.billing.repository.BillingRecordRepository
import com.duoc.app.features.report.dto.*
import com.duoc.app.features.reservation.repository.ReservationRepository
import com.duoc.app.features.professionalprofile.repository.ProfessionalProfileRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.*

@Service
class ReportService(
    private val reservationRepository: ReservationRepository,
    private val attentionRepository: AttentionRepository,
    private val billingRecordRepository: BillingRecordRepository,
    private val professionalProfileRepository: ProfessionalProfileRepository
) {

    fun getSummaryBySpecialist(userId: String): ReportSummaryResponse {
        val now = LocalDateTime.now()
        val monthStart = now.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN)
        val monthEnd = now.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX)

        val totalReservations = reservationRepository.countBySpecialist_IdAndReservationStartBetween(userId, monthStart, monthEnd).toInt()
        val completedAttentionsCount = attentionRepository.countBySpecialist_IdAndStartedAtBetweenAndStatus(
            userId, monthStart, monthEnd, AttentionStatus.FINISHED
        ).toInt()
        
        val finishedAttentionsSum = attentionRepository.sumDurationMinutesBySpecialistAndStartedAtBetweenAndStatus(
            userId, monthStart, monthEnd, AttentionStatus.FINISHED
        ) ?: 0L
        
        val completedReservationsSum = reservationRepository.sumServiceDurationMinutesBySpecialistAndReservationStartBetweenAndStatus(
            userId, monthStart, monthEnd, com.duoc.app.features.reservation.model.ReservationStatus.COMPLETED
        ) ?: 0L

        val totalCompletedCount = attentionRepository.countBySpecialist_IdAndStartedAtBetweenAndStatus(
            userId, monthStart, monthEnd, AttentionStatus.FINISHED
        ) + reservationRepository.countBySpecialist_IdAndReservationStartBetweenAndStatus(
            userId, monthStart, monthEnd, com.duoc.app.features.reservation.model.ReservationStatus.COMPLETED
        )

        val avgMinutes = if (totalCompletedCount > 0) {
            (finishedAttentionsSum + completedReservationsSum).toDouble() / totalCompletedCount
        } else 0.0

        val today = LocalDate.now()
        val todayReservationsCount = reservationRepository.countBySpecialist_IdAndReservationStartBetween(
            userId, today.atStartOfDay(), today.atTime(LocalTime.MAX)
        ).toInt()

        val totalCharged = billingRecordRepository.sumAmountBySpecialistAndCreatedAtBetweenAndStatus(
            userId, monthStart, monthEnd, PaymentStatus.PAID
        ) ?: BigDecimal.ZERO

        val pendingAmount = billingRecordRepository.sumAmountBySpecialistAndCreatedAtBetweenAndStatus(
            userId, monthStart, monthEnd, PaymentStatus.PENDING
        ) ?: BigDecimal.ZERO

        val paidBillingCount = billingRecordRepository.countBySpecialist_IdAndCreatedAtBetweenAndStatus(
            userId, monthStart, monthEnd, PaymentStatus.PAID
        ).toInt()

        val pendingBillingCount = billingRecordRepository.countBySpecialist_IdAndCreatedAtBetweenAndStatus(
            userId, monthStart, monthEnd, PaymentStatus.PENDING
        ).toInt()

        return ReportSummaryResponse(
            totalReservations = totalReservations,
            todayReservations = todayReservationsCount,
            completedAttentions = completedAttentionsCount,
            averageAttentionMinutes = avgMinutes,
            totalCharged = totalCharged,
            pendingAmount = pendingAmount,
            paidBillingCount = paidBillingCount,
            pendingBillingCount = pendingBillingCount
        )
    }

    fun getWeeklyReport(userId: String, weekOffset: Long = 0, serviceId: String? = null): WeeklyReportResponse {
        if (professionalProfileRepository.findByUser_Id(userId) == null) {
            return WeeklyReportResponse(0, 0, 0, 0, 0.0, 0.0, 0.0, emptyList())
        }
        
        val now = LocalDate.now().minusWeeks(weekOffset)
        val weekFields = WeekFields.of(Locale.getDefault())
        val firstDayOfWeek = now.with(weekFields.dayOfWeek(), 1L)
        val lastDayOfWeek = now.with(weekFields.dayOfWeek(), 7L)

        val start = firstDayOfWeek.atStartOfDay()
        val end = lastDayOfWeek.atTime(LocalTime.MAX)

        val reservations = if (serviceId != null) {
            reservationRepository.findBySpecialist_IdAndReservationStartBetweenAndService_Id(userId, start, end, serviceId)
        } else {
            reservationRepository.findBySpecialist_IdAndReservationStartBetween(userId, start, end)
        }

        val billing = if (serviceId != null) {
            billingRecordRepository.findBySpecialist_IdAndCreatedAtBetweenAndReservation_Service_Id(userId, start, end, serviceId)
        } else {
            billingRecordRepository.findBySpecialist_IdAndCreatedAtBetween(userId, start, end)
        }

        val prevStart = firstDayOfWeek.minusWeeks(1).atStartOfDay()
        val prevEnd = lastDayOfWeek.minusWeeks(1).atTime(LocalTime.MAX)
        
        val prevRevenue = billingRecordRepository.sumAmountBySpecialistAndCreatedAtBetweenAndStatus(userId, prevStart, prevEnd, PaymentStatus.PAID)
            ?: BigDecimal.ZERO

        val totalMinutes = (attentionRepository.sumDurationMinutesBySpecialistAndStartedAtBetweenAndStatus(
            userId, start, end, AttentionStatus.FINISHED
        ) ?: 0L) + (reservationRepository.sumServiceDurationMinutesBySpecialistAndReservationStartBetweenAndStatus(
            userId, start, end, com.duoc.app.features.reservation.model.ReservationStatus.COMPLETED
        ) ?: 0L)

        val dailyMetrics = mutableListOf<DailyMetricDto>()
        var currentDay = firstDayOfWeek

        while (!currentDay.isAfter(lastDayOfWeek)) {
            val dayReservations = reservations.filter { it.reservationStart.toLocalDate() == currentDay }
            val dayRevenue = billing
                .filter { it.createdAt.toLocalDate() == currentDay && it.status == PaymentStatus.PAID }
                .sumOf { it.amount.toDouble() }

            dailyMetrics.add(DailyMetricDto(
                dayOfWeek = currentDay.dayOfWeek.name,
                date = currentDay.toString(),
                reservationsCount = dayReservations.size,
                revenue = dayRevenue
            ))
            currentDay = currentDay.plusDays(1)
        }

        return WeeklyReportResponse(
            weekNumber = now.get(weekFields.weekOfYear()),
            year = now.year,
            totalReservations = reservations.size,
            completedAttentions = reservations.count { 
                it.status == com.duoc.app.features.reservation.model.ReservationStatus.COMPLETED 
            },
            totalRevenue = dailyMetrics.sumOf { it.revenue },
            totalHoursWorked = totalMinutes.toDouble() / 60.0,
            previousPeriodRevenue = prevRevenue.toDouble(),
            dailyBreakdown = dailyMetrics
        )
    }

    fun getMonthlyReport(userId: String, monthOffset: Long = 0, serviceId: String? = null): MonthlyReportResponse {
        if (professionalProfileRepository.findByUser_Id(userId) == null) {
            return MonthlyReportResponse("N/A", 0, 0, 0, 0.0, 0.0, 0.0, emptyList())
        }
        
        val now = LocalDate.now().minusMonths(monthOffset)
        val monthStart = now.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay()
        val monthEnd = now.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX)

        val reservations = if (serviceId != null) {
            reservationRepository.findBySpecialist_IdAndReservationStartBetweenAndService_Id(userId, monthStart, monthEnd, serviceId)
        } else {
            reservationRepository.findBySpecialist_IdAndReservationStartBetween(userId, monthStart, monthEnd)
        }

        val billing = if (serviceId != null) {
            billingRecordRepository.findBySpecialist_IdAndCreatedAtBetweenAndReservation_Service_Id(userId, monthStart, monthEnd, serviceId)
        } else {
            billingRecordRepository.findBySpecialist_IdAndCreatedAtBetween(userId, monthStart, monthEnd)
        }

        val prevStart = monthStart.minusMonths(1)
        val prevEnd = monthEnd.minusMonths(1)

        val prevRevenue = billingRecordRepository.sumAmountBySpecialistAndCreatedAtBetweenAndStatus(userId, prevStart, prevEnd, PaymentStatus.PAID)
            ?: BigDecimal.ZERO

        val totalMinutes = (attentionRepository.sumDurationMinutesBySpecialistAndStartedAtBetweenAndStatus(
            userId, monthStart, monthEnd, AttentionStatus.FINISHED
        ) ?: 0L) + (reservationRepository.sumServiceDurationMinutesBySpecialistAndReservationStartBetweenAndStatus(
            userId, monthStart, monthEnd, com.duoc.app.features.reservation.model.ReservationStatus.COMPLETED
        ) ?: 0L)

        val weeklySummaries = mutableListOf<WeeklySummaryDto>()
        var current = monthStart.toLocalDate()
        val weekFields = WeekFields.of(Locale.getDefault())

        while (!current.isAfter(monthEnd.toLocalDate())) {
            val weekNum = current.get(weekFields.weekOfYear())
            val weekStart = current.with(weekFields.dayOfWeek(), 1L)
            val weekEnd = current.with(weekFields.dayOfWeek(), 7L)
            
            val reservationsInWeek = reservations.filter { 
                !it.reservationStart.toLocalDate().isBefore(weekStart) && !it.reservationStart.toLocalDate().isAfter(weekEnd)
            }
            val revenueInWeek = billing.filter { 
                !it.createdAt.toLocalDate().isBefore(weekStart) && !it.createdAt.toLocalDate().isAfter(weekEnd) && it.status == PaymentStatus.PAID
            }.sumOf { it.amount.toDouble() }

            weeklySummaries.add(WeeklySummaryDto(
                weekNumber = weekNum,
                dateRange = "${weekStart.dayOfMonth}/${weekStart.monthValue} - ${weekEnd.dayOfMonth}/${weekEnd.monthValue}",
                reservationsCount = reservationsInWeek.size,
                revenue = revenueInWeek
            ))
            
            current = weekEnd.plusDays(1)
            if (current.month != now.month && current.isAfter(monthEnd.toLocalDate())) break
        }

        return MonthlyReportResponse(
            monthName = now.month.name,
            year = now.year,
            totalReservations = reservations.size,
            completedAttentions = reservations.count { 
                it.status == com.duoc.app.features.reservation.model.ReservationStatus.COMPLETED 
            },
            totalRevenue = weeklySummaries.sumOf { it.revenue },
            totalHoursWorked = totalMinutes.toDouble() / 60.0,
            previousPeriodRevenue = prevRevenue.toDouble(),
            weeklyBreakdown = weeklySummaries
        )
    }

    fun exportWeeklyCSV(userId: String, weekOffset: Long, serviceId: String? = null): String {
        val report = getWeeklyReport(userId, weekOffset, serviceId)
        val sb = StringBuilder()
        sb.append("Reporte Semanal;Semana ${report.weekNumber};${report.year}\n")
        sb.append("Total Reservas;${report.totalReservations}\n")
        sb.append("Ingresos Totales;${report.totalRevenue}\n\n")
        sb.append("Fecha;Dia;Reservas;Ingresos\n")
        report.dailyBreakdown.forEach { 
            sb.append("${it.date};${it.dayOfWeek};${it.reservationsCount};${it.revenue}\n")
        }
        return sb.toString()
    }

    fun exportMonthlyCSV(userId: String, monthOffset: Long, serviceId: String? = null): String {
        val report = getMonthlyReport(userId, monthOffset, serviceId)
        val sb = StringBuilder()
        sb.append("Reporte Mensual;${report.monthName};${report.year}\n")
        sb.append("Total Reservas;${report.totalReservations}\n")
        sb.append("Ingresos Totales;${report.totalRevenue}\n\n")
        sb.append("Semana;Rango;Reservas;Ingresos\n")
        report.weeklyBreakdown.forEach { 
            sb.append("${it.weekNumber};${it.dateRange};${it.reservationsCount};${it.revenue}\n")
        }
        return sb.toString()
    }
}
