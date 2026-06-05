package com.pointcheck.features.billing.data.dto

import com.pointcheck.core.data.dto.UserSummaryDto

/**
 * DTO para el registro de facturación.
 * Actualizado para usar UserSummaryDto.
 */
data class BillingRecordResponseDto(
    val id: String,
    val reservationId: String,
    val attentionId: String?,
    val client: UserSummaryDto,
    val specialist: UserSummaryDto,
    val amount: Double,
    val currency: String,
    val paymentMethod: String?,
    val status: String,
    val paidAt: String?,
    val externalReference: String?,
    val notes: String?,
    val createdAt: String
)
