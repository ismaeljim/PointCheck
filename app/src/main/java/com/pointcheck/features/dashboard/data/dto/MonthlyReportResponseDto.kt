package com.pointcheck.features.dashboard.data.dto

data class MonthlyReportResponseDto(
    val monthName: String,
    val year: Int,
    val totalReservations: Int,
    val completedAttentions: Int,
    val totalRevenue: Double,
    val totalHoursWorked: Double,
    val previousPeriodRevenue: Double,
    val weeklyBreakdown: List<WeeklySummaryDto>
)

data class WeeklySummaryDto(
    val weekNumber: Int,
    val dateRange: String,
    val reservationsCount: Int,
    val revenue: Double
)
