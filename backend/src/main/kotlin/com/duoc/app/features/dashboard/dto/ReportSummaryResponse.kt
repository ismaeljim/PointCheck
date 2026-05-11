package com.duoc.app.features.dashboard.dto

data class ReportSummaryResponse(
    val totalReservations: Int = 0,
    val todayReservations: Int = 0,
    val completedAttentions: Int = 0,
    val averageAttentionMinutes: Double = 0.0,
    val totalCharged: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val paidBillingCount: Int = 0,
    val pendingBillingCount: Int = 0
)
