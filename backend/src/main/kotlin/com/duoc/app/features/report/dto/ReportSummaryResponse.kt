package com.duoc.app.features.report.dto

data class ReportSummaryResponse(
<<<<<<< Updated upstream
    val totalReservations: Int = 0,
    val todayReservations: Int = 0,
    val completedAttentions: Int = 0,
    val averageAttentionMinutes: Double = 0.0,
    val totalCharged: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val paidBillingCount: Int = 0,
    val pendingBillingCount: Int = 0
=======
    val specialistId: String,
    val totalReservations: Int,
    val todayReservations: Int,
    val completedAttentions: Int,
    val averageAttentionMinutes: Double,
    val totalCharged: BigDecimal,
    val pendingAmount: BigDecimal,
    val paidBillingCount: Int,
    val pendingBillingCount: Int
>>>>>>> Stashed changes
)
