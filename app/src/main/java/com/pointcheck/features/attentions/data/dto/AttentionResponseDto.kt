package com.pointcheck.features.attentions.data.dto

import com.pointcheck.core.data.dto.UserSummaryDto

/**
 * DTO para la respuesta de una atención.
 * Actualizado para usar UserSummaryDto.
 */
data class AttentionResponseDto(
    val id: String,
    val reservationId: String,
    val client: UserSummaryDto,
    val specialist: UserSummaryDto,
    val startedAt: String,
    val finishedAt: String?,
    val durationMinutes: Int?,
    val status: String,
    val observations: String?,
    val createdAt: String
)
