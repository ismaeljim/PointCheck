package com.pointcheck.features.attentions.data.dto

data class AttentionResponseDto(
    val id: Long,
    val reservationId: Long,
    val clientId: Long,
    val specialistId: Long,
    val startedAt: String,
    val finishedAt: String? = null,
    val durationMinutes: Int? = null,
    val status: String,
    val observations: String? = null
)
