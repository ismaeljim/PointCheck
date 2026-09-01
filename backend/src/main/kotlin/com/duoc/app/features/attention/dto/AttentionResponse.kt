package com.duoc.app.features.attention.dto

import com.duoc.app.features.attention.model.AttentionStatus
import com.duoc.app.features.user.dto.UserSummaryDto
import java.time.LocalDateTime

data class AttentionResponse(
    val id: String,
    val reservationId: String,
    val client: UserSummaryDto,
    val specialist: UserSummaryDto,
    val specialistProfileId: String = "",
    val startedAt: LocalDateTime,
    val finishedAt: LocalDateTime?,
    val durationMinutes: Int?,
    val status: AttentionStatus,
    val observations: String?,
    val createdAt: LocalDateTime
)
