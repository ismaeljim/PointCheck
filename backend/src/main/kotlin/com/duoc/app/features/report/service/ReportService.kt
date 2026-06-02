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

/**
 * AUDITORÍA TÉCNICA: Motor de Inteligencia de Negocio y Reportabilidad
 * 
 * Este servicio procesa grandes volúmenes de datos de reservas, atenciones y facturación
 * para generar métricas clave de desempeño (KPIs) para los especialistas.
 * 
 * Hallazgos de Implementación:
 * 1. [OK] Granularidad Temporal: Soporte para reportes Diarios, Semanales y Mensuales con 'offsets'.
 * 2. [OK] Lógica de Negocio: Cálculo de ingresos reales vs adeudados y promedios de duración de atención.
 * 3. [INFO] Rendimiento: El cálculo se realiza en memoria sobre colecciones filtradas. 
 *    Para grandes volúmenes de datos históricos, se recomienda migrar a agregaciones a nivel de base de datos (SQL native queries).
 * 4. [OK] Exportabilidad: Generación nativa de formato CSV para interoperabilidad con Excel/Sheets.
 */
@Service
class ReportService(
    private val reservationRepository: ReservationRepository,
    private val attentionRepository: AttentionRepository,
    private val billingRecordRepository: BillingRecordRepository,
    private val professionalProfileRepository: ProfessionalProfileRepository
) {

    /**
     * AUDITORÍA: Dashboard de Resumen.
     * Consolida métricas del mes en curso para una visualización rápida.
     */
    fun getSummaryBySpecialist(userId: String): ReportSummaryResponse {
        val now = LocalDateTime.now()
        val monthStart = now.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN)
        val monthEnd = now.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX)

        val monthAttentions = attentionRepository.findBySpecialist_IdAndStartedAtBetween(userId, monthStart, monthEnd)
        val finishedAttentions = monthAttentions.filter { it.status == AttentionStatus.FINISHED }
        
        val monthReservations = reservationRepository.findBySpecialist_IdAndReservationStartBetween(userId, monthStart, monthEnd)
        val completedReservations = monthReservations.filter { it.status == com.duoc.app.features.reservation.model.ReservationStatus.COMPLETED }

        val allDurations = mutableListOf<Double>()
        finishedAttentions.forEach { it.durationMinutes?.let { d -> allDurations.add(d.toDouble()) } }
        completedReservations.forEach { res ->
            res.service?.durationMinutes?.let { d -> allDurations.add(d.toDouble()) }
        }

        val avgMinutes = if (allDurations.isNotEmpty()) allDurations.average() else 0.0

        val today = LocalDate.now()
        val todayReservations = monthReservations.filter { it.reservationStart.toLocalDate() == today }

        val allBilling = billingRecordRepository.findBySpecialist_IdAndCreatedAtBetween(userId, monthStart, monthEnd)
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
            totalCharged = totalCharged,
            pendingAmount = pendingAmount,
            paidBillingCount = paidBilling.size,
            pendingBillingCount = pendingBilling.size
        )
    }

    /**
     * AUDITORÍA: Desglose Semanal con Filtro de Servicio.
     * Permite comparar ingresos del periodo actual vs el anterior.
     */
    fun getWeeklyReport(userId: String, weekOffset: Long = 0, serviceId: String? = null): WeeklyReportResponse {
        val profile = professionalProfileRepository.findByUser_Id(userId)
            ?: return WeeklyReportResponse(0, 0, 0, 0, 0.0, 0.0, 0.0, emptyList())
        
        val specialistUserId = profile.user.id!!
        val now = LocalDate.now().minusWeeks(weekOffset)
        val weekFields = WeekFields.of(Locale.getDefault())
        val firstDayOfWeek = now.with(weekFields.dayOfWeek(), 1L)
        val lastDayOfWeek = now.with(weekFields.dayOfWeek(), 7L)

        val start = firstDayOfWeek.atStartOfDay()
        val end = lastDayOfWeek.atTime(LocalTime.MAX)

        var reservations = reservationRepository.findBySpecialist_IdAndReservationStartBetween(specialistUserId, start, end)
        var billing = billingRecordRepository.findBySpecialist_IdAndCreatedAtBetween(specialistUserId, start, end)

        if (serviceId != null) {
            reservations = reservations.filter { it.service?.id == serviceId }
            billing = billing.filter { it.reservation.service?.id == serviceId }
        }

        val prevStart = firstDayOfWeek.minusWeeks(1).atStartOfDay()
        val prevEnd = lastDayOfWeek.minusWeeks(1).atTime(LocalTime.MAX)
        var prevBilling = billingRecordRepository.findBySpecialist_IdAndCreatedAtBetween(specialistUserId, prevStart, prevEnd)
            .filter { it.status == PaymentStatus.PAID }
        
        if (serviceId != null) {
            prevBilling = prevBilling.filter { it.reservation.service?.id == serviceId }
        }
        val prevRevenue = prevBilling.sumOf { it.amount.toDouble() }

        val dailyMetrics = mutableListOf<DailyMetricDto>()
        var currentDay = firstDayOfWeek
        var totalHours = 0.0

        while (!currentDay.isAfter(lastDayOfWeek)) {
            val dayReservations = reservations.filter { it.reservationStart.toLocalDate() == currentDay }
            val dayRevenue = billing
                .filter { it.createdAt.toLocalDate() == currentDay && it.status == PaymentStatus.PAID }
                .sumOf { it.amount.toDouble() }

            dayReservations.forEach { res ->
                if (res.status.name == "COMPLETED" || res.status.name == "FINISHED") {
                    totalHours += (res.service?.durationMinutes ?: 0).toDouble() / 60.0
                }
            }

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
            completedAttentions = reservations.count { it.status.name == "COMPLETED" || it.status.name == "FINISHED" },
            totalRevenue = dailyMetrics.sumOf { it.revenue },
            totalHoursWorked = totalHours,
            previousPeriodRevenue = prevRevenue,
            dailyBreakdown = dailyMetrics
        )
    }

    fun getMonthlyReport(userId: String, monthOffset: Long = 0, serviceId: String? = null): MonthlyReportResponse {
        val profile = professionalProfileRepository.findByUser_Id(userId)
            ?: return MonthlyReportResponse("N/A", 0, 0, 0, 0.0, 0.0, 0.0, emptyList())
        
        val specialistUserId = profile.user.id!!
        val now = LocalDate.now().minusMonths(monthOffset)
        val monthStart = now.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay()
        val monthEnd = now.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX)

        var reservations = reservationRepository.findBySpecialist_IdAndReservationStartBetween(specialistUserId, monthStart, monthEnd)
        var billing = billingRecordRepository.findBySpecialist_IdAndCreatedAtBetween(specialistUserId, monthStart, monthEnd)

        if (serviceId != null) {
            reservations = reservations.filter { it.service?.id == serviceId }
            billing = billing.filter { it.reservation?.service?.id == serviceId }
        }

        val prevStart = monthStart.minusMonths(1)
        val prevEnd = monthEnd.minusMonths(1)
        var prevBilling = billingRecordRepository.findBySpecialist_IdAndCreatedAtBetween(specialistUserId, prevStart, prevEnd)
            .filter { it.status == PaymentStatus.PAID }
        
        if (serviceId != null) {
            prevBilling = prevBilling.filter { it.reservation?.service?.id == serviceId }
        }
        val prevRevenue = prevBilling.sumOf { it.amount.toDouble() }

        val weeklySummaries = mutableListOf<WeeklySummaryDto>()
        var current = monthStart.toLocalDate()
        val weekFields = WeekFields.of(Locale.getDefault())
        var totalHours = 0.0

        reservations.forEach { res ->
            if (res.status.name == "COMPLETED" || res.status.name == "FINISHED") {
                totalHours += (res.service?.durationMinutes ?: 0).toDouble() / 60.0
            }
        }

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
            completedAttentions = reservations.count { it.status.name == "COMPLETED" || it.status.name == "FINISHED" },
            totalRevenue = weeklySummaries.sumOf { it.revenue },
            totalHoursWorked = totalHours,
            previousPeriodRevenue = prevRevenue,
            weeklyBreakdown = weeklySummaries
        )
    }

    /**
     * AUDITORÍA: Generador CSV.
     * Implementa formato semi-colon (;) común en regiones latinas para correcta apertura en Excel.
     */
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
