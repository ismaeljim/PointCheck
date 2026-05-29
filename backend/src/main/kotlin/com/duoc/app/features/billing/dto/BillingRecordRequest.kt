package com.duoc.app.features.billing.dto

import com.duoc.app.features.billing.model.PaymentMethod
import java.math.BigDecimal

data class BillingRecordRequest(
    val reservationId: String,
    val attentionId: String? = null,
    val amount: BigDecimal,
    val currency: String = "CLP",
    val paymentMethod: PaymentMethod? = null,
    val notes: String? = null
)
