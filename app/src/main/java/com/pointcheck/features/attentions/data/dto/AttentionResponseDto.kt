package com.pointcheck.features.attentions.data.dto

import com.pointcheck.core.data.dto.UserSummaryDto

/**
 * DTO de Atención blindado contra nulos para Jetpack Compose.
 */
data class AttentionResponseDto(
    val id: String,
    val reservationId: String,
    val specialistProfileId: String = "",
    val client: UserSummaryDto,
    val specialist: UserSummaryDto,
    val startedAt: String,
    val finishedAt: String = "",
    val durationMinutes: Int = 0,
    val status: String,
    val observations: String = "",
    val createdAt: String
)
