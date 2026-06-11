package com.pointcheck.features.dashboard.data.dto

data class WeeklyReportResponseDto(
    val weekNumber: Int = 0,
    val year: Int = 0,
    val totalReservations: Int = 0,
    val completedAttentions: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalHoursWorked: Double = 0.0,
    val previousPeriodRevenue: Double = 0.0,
    val dailyBreakdown: List<DailyMetricDto> = emptyList()
)

data class DailyMetricDto(
    val dayOfWeek: String = "",
    val date: String = "",
    val reservationsCount: Int = 0,
    val revenue: Double = 0.0
)
