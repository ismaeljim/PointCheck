package com.pointcheck.features.billing.data.dto

import com.pointcheck.core.data.dto.UserSummaryDto

/**
 * DTO de Facturación blindado contra nulos para Jetpack Compose.
 */
data class BillingRecordResponseDto(
    val id: String,
    val reservationId: String,
    val attentionId: String? = null,
    val client: UserSummaryDto,
    val specialist: UserSummaryDto,
    val amount: Double,
    val currency: String = "CLP",
    val paymentMethod: String = "",
    val status: String,
    val paidAt: String? = null,
    val externalReference: String = "",
    val notes: String = "",
    val createdAt: String
)
