package com.pointcheck.features.dashboard.data.dto

data class WeeklyReportResponseDto(
    val weekNumber: Int,
    val year: Int,
    val totalReservations: Int,
    val completedAttentions: Int,
    val totalRevenue: Double,
    val dailyBreakdown: List<DailyMetricDto>
)

data class DailyMetricDto(
    val dayOfWeek: String,
    val date: String,
    val reservationsCount: Int,
    val revenue: Double
)
