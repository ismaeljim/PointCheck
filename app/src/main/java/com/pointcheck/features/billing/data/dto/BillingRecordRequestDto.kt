package com.pointcheck.features.billing.data.dto

data class BillingRecordRequestDto(
    val reservationId: String,
    val attentionId: String? = null,
    val amount: Double,
    val currency: String = "CLP",
    val paymentMethod: String? = null,
    val notes: String? = null
)
