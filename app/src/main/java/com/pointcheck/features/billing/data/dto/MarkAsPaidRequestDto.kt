package com.pointcheck.features.billing.data.dto

data class MarkAsPaidRequestDto(
    val paymentMethod: String,
    val externalReference: String? = null,
    val notes: String? = null
)
