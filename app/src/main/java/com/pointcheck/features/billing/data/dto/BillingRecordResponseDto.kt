package com.pointcheck.features.billing.data.dto

data class BillingRecordResponseDto(
    val id: Long,
    val reservationId: Long,
    val attentionId: Long?,
    val clientId: Long,
    val specialistId: Long,
    val amount: Double,
    val currency: String,
    val paymentMethod: String?,
    val status: String,
    val paidAt: String?,
    val externalReference: String?,
    val notes: String?,
    val createdAt: String
)
