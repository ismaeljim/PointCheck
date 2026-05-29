package com.pointcheck.features.billing.data.dto

data class BillingRecordResponseDto(
    val id: String,
    val reservationId: String,
    val attentionId: String?,
    val clientId: String,
    val specialistId: String,
    val amount: Double,
    val currency: String,
    val paymentMethod: String?,
    val status: String,
    val paidAt: String?,
    val externalReference: String?,
    val notes: String?,
    val createdAt: String
)
