package com.duoc.app.features.attention.dto

import com.duoc.app.features.attention.model.AttentionStatus
import java.time.LocalDateTime

data class AttentionResponse(
    val id: Long,
    val reservationId: Long,
    val clientId: Long,
    val specialistId: Long,
    val startedAt: LocalDateTime,
    val finishedAt: LocalDateTime?,
    val durationMinutes: Int?,
    val status: AttentionStatus,
    val observations: String?,
    val createdAt: LocalDateTime
)
