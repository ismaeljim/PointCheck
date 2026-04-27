package com.pointcheck.features.billing.data.dto

data class BillingRecordResponseDto(
    val id: Long,
    val reservationId: Long,
    val attentionId: Long? = null,
    val clientId: Long,
    val specialistId: Long,
    val amount: Double,
    val currency: String,
    val paymentMethod: String,
    val status: String,
    val paidAt: String? = null,
    val externalReference: String? = null,
    val notes: String? = null
)
