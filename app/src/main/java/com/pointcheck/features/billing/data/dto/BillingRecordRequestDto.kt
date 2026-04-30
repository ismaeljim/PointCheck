package com.pointcheck.features.billing.data.dto

data class BillingRecordRequestDto(
    val reservationId: Long,
    val attentionId: Long? = null,
    val amount: Double,
    val currency: String = "CLP",
    val paymentMethod: String? = null,
    val notes: String? = null
)
