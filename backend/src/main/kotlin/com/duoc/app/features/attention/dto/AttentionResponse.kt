package com.duoc.app.features.attention.dto

import com.duoc.app.features.attention.model.AttentionStatus
import java.time.LocalDateTime

data class AttentionResponse(
    val id: String?,
    val reservationId: String?,
    val clientId: String?,
    val specialistId: String?,
    val startedAt: LocalDateTime,
    val finishedAt: LocalDateTime?,
    val durationMinutes: Int?,
    val status: AttentionStatus,
    val observations: String?,
    val createdAt: LocalDateTime
)
