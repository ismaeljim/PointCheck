package com.pointcheck.features.billing.data.dto

data class BillingRecordRequestDto(
    val reservationId: Long,
    val attentionId: Long? = null,
    val clientId: Long,
    val specialistId: Long,
    val amount: Double,
    val currency: String = "CLP",
    val paymentMethod: String,
    val status: String = "PENDING",
    val externalReference: String? = null,
    val notes: String? = null
)
