package com.pointcheck.features.dashboard.data.dto

/**
 * DTO para resumen de reportes blindado contra nulos.
 */
data class ReportSummaryResponseDto(
    val specialistProfileId: String? = "",
    val totalReservations: Int? = 0,
    val todayReservations: Int? = 0,
    val completedAttentions: Int? = 0,
    val averageAttentionMinutes: Double? = 0.0,
    val totalCharged: Double? = 0.0,
    val pendingAmount: Double? = 0.0,
    val paidBillingCount: Int? = 0,
    val pendingBillingCount: Int? = 0,
    val specialty: String? = ""
)
