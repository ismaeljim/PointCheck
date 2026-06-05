package com.duoc.app.features.billing.dto

import com.duoc.app.features.billing.model.PaymentMethod
import com.duoc.app.features.billing.model.PaymentStatus
import com.duoc.app.features.user.dto.UserSummaryDto
import java.math.BigDecimal
import java.time.LocalDateTime

data class BillingRecordResponse(
    val id: String,
    val reservationId: String,
    val attentionId: String?,
    val client: UserSummaryDto,
    val specialist: UserSummaryDto,
    val amount: BigDecimal,
    val currency: String,
    val paymentMethod: PaymentMethod?,
    val status: PaymentStatus,
    val paidAt: LocalDateTime?,
    val externalReference: String?,
    val notes: String?,
    val createdAt: LocalDateTime
)
