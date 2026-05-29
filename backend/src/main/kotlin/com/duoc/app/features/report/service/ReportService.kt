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

<<<<<<< Updated upstream
    fun getSummaryBySpecialist(userId: Long): ReportSummaryResponse {
        val profile = professionalProfileRepository.findByUser_Id(userId) 
            ?: return ReportSummaryResponse()
        
        val specialistId = profile.id
        val now = LocalDateTime.now()
        val monthStart = now.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN)
        val monthEnd = now.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX)
=======
    fun getSummaryBySpecialist(specialistId: String): ReportSummaryResponse {
        // Reservas
        val allReservations = reservationRepository.findBySpecialist_Id(specialistId)
        val today = LocalDate.now()
        val todayReservations = allReservations.filter { it.reservationStart.toLocalDate() == today }
>>>>>>> Stashed changes

        val monthReservations = reservationRepository.findBySpecialist_IdAndReservationStartBetween(specialistId, monthStart, monthEnd)
        val today = LocalDate.now()
        val todayReservations = monthReservations.filter { it.reservationStart.toLocalDate() == today }

        val monthAttentions = attentionRepository.findBySpecialist_IdAndStartedAtBetween(specialistId, monthStart, monthEnd)
        val finishedAttentions = monthAttentions.filter { it.status == AttentionStatus.FINISHED }
        val avgMinutes = if (finishedAttentions.isNotEmpty()) {
            finishedAttentions.mapNotNull { it.durationMinutes }.average()
        } else {
            0.0
        }

        val allBilling = billingRecordRepository.findBySpecialist_IdAndCreatedAtBetween(specialistId, monthStart, monthEnd)
        val paidBilling = allBilling.filter { it.status == PaymentStatus.PAID }
        val pendingBilling = allBilling.filter { it.status == PaymentStatus.PENDING }

        val totalCharged = paidBilling
            .map { it.amount }
            .fold(BigDecimal.ZERO, BigDecimal::add)

        val pendingAmount = pendingBilling
            .map { it.amount }
            .fold(BigDecimal.ZERO, BigDecimal::add)

        return ReportSummaryResponse(
            totalReservations = monthReservations.size,
            todayReservations = todayReservations.size,
            completedAttentions = finishedAttentions.size,
            averageAttentionMinutes = avgMinutes,
            totalCharged = totalCharged.toDouble(),
            pendingAmount = pendingAmount.toDouble(),
            paidBillingCount = paidBilling.size,
            pendingBillingCount = pendingBilling.size
        )
    }

    fun getWeeklyReport(userId: Long, weekOffset: Long = 0): WeeklyReportResponse {
        val profile = professionalProfileRepository.findByUser_Id(userId)
            ?: return WeeklyReportResponse(0, 0, 0, 0, 0.0, emptyList())
        
        val specialistId = profile.id
        val now = LocalDate.now().minusWeeks(weekOffset)
        val weekFields = WeekFields.of(Locale.getDefault())
        val firstDayOfWeek = now.with(weekFields.dayOfWeek(), 1L)
        val lastDayOfWeek = now.with(weekFields.dayOfWeek(), 7L)

        val start = firstDayOfWeek.atStartOfDay()
        val end = lastDayOfWeek.atTime(LocalTime.MAX)

        val reservations = reservationRepository.findBySpecialist_IdAndReservationStartBetween(specialistId, start, end)
        val billing = billingRecordRepository.findBySpecialist_IdAndCreatedAtBetween(specialistId, start, end)

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
            completedAttentions = reservations.count { it.status?.name == "COMPLETED" || it.status?.name == "FINISHED" },
            totalRevenue = dailyMetrics.sumOf { it.revenue },
            dailyBreakdown = dailyMetrics
        )
    }

    fun getMonthlyReport(userId: Long, monthOffset: Long = 0): MonthlyReportResponse {
        val profile = professionalProfileRepository.findByUser_Id(userId)
            ?: return MonthlyReportResponse("N/A", 0, 0, 0, 0.0, emptyList())
        
        val specialistId = profile.id
        val now = LocalDate.now().minusMonths(monthOffset)
        val monthStart = now.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay()
        val monthEnd = now.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX)

        val reservations = reservationRepository.findBySpecialist_IdAndReservationStartBetween(specialistId, monthStart, monthEnd)
        val billing = billingRecordRepository.findBySpecialist_IdAndCreatedAtBetween(specialistId, monthStart, monthEnd)

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
            // Si nos pasamos del mes, terminamos el desglose
            if (current.month != now.month && current.isAfter(monthEnd.toLocalDate())) break
        }

        return MonthlyReportResponse(
            monthName = now.month.name,
            year = now.year,
            totalReservations = reservations.size,
            completedAttentions = reservations.count { it.status?.name == "COMPLETED" || it.status?.name == "FINISHED" },
            totalRevenue = weeklySummaries.sumOf { it.revenue },
            weeklyBreakdown = weeklySummaries
        )
    }

    fun exportWeeklyCSV(userId: Long, weekOffset: Long): String {
        val report = getWeeklyReport(userId, weekOffset)
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

    fun exportMonthlyCSV(userId: Long, monthOffset: Long): String {
        val report = getMonthlyReport(userId, monthOffset)
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
