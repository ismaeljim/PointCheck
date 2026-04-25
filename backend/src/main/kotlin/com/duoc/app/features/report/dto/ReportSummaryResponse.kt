package com.duoc.app.features.report.dto

import java.math.BigDecimal

data class ReportSummaryResponse(
    val specialistId: Long,
    val totalReservations: Int,
    val todayReservations: Int,
    val completedAttentions: Int,
    val averageAttentionMinutes: Double,
    val totalCharged: BigDecimal,
    val pendingAmount: BigDecimal,
    val paidBillingCount: Int,
    val pendingBillingCount: Int
)
