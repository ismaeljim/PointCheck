package com.duoc.app.features.billing.dto

import com.duoc.app.features.billing.model.PaymentMethod

data class MarkAsPaidRequest(
    val paymentMethod: PaymentMethod,
    val externalReference: String? = null,
    val notes: String? = null
)
