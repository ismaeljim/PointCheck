package com.duoc.app.features.billing.dto

import com.duoc.app.features.billing.model.PaymentMethod
import com.duoc.app.features.billing.model.PaymentStatus
import java.math.BigDecimal
import java.time.LocalDateTime

data class BillingRecordResponse(
    val id: Long,
    val reservationId: Long,
    val attentionId: Long?,
    val clientId: Long,
    val specialistId: Long,
    val amount: BigDecimal,
    val currency: String,
    val paymentMethod: PaymentMethod?,
    val status: PaymentStatus,
    val paidAt: LocalDateTime?,
    val externalReference: String?,
    val notes: String?,
    val createdAt: LocalDateTime
)
