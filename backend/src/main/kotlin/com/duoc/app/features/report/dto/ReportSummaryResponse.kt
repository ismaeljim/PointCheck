package com.duoc.app.features.report.dto

import java.math.BigDecimal

data class ReportSummaryResponse(
    val totalReservations: Int = 0,
    val todayReservations: Int = 0,
    val completedAttentions: Int = 0,
    val averageAttentionMinutes: Double = 0.0,
    val totalCharged: BigDecimal = BigDecimal.ZERO,
    val pendingAmount: BigDecimal = BigDecimal.ZERO,
    val paidBillingCount: Int = 0,
    val pendingBillingCount: Int = 0,
    val specialty: String? = null
)
