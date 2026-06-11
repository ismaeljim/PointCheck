package com.duoc.app.features.report.dto

data class MonthlyReportResponse(
    val monthName: String = "",
    val year: Int = 0,
    val totalReservations: Int = 0,
    val completedAttentions: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalHoursWorked: Double = 0.0,
    val previousPeriodRevenue: Double = 0.0,
    val weeklyBreakdown: List<WeeklySummaryDto> = emptyList()
)

data class WeeklySummaryDto(
    val weekNumber: Int = 0,
    val dateRange: String = "",
    val reservationsCount: Int = 0,
    val revenue: Double = 0.0
)
