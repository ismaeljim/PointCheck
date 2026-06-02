package com.pointcheck.features.dashboard.data.dto

data class ReportSummaryResponseDto(
    val specialistId: String,
    val totalReservations: Int,
    val todayReservations: Int,
    val completedAttentions: Int,
    val averageAttentionMinutes: Double,
    val totalCharged: Double,
    val pendingAmount: Double,
    val paidBillingCount: Int,
    val pendingBillingCount: Int,
    val specialty: String? = null
)
