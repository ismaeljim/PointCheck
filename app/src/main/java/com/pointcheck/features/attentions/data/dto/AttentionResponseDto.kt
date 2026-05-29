package com.pointcheck.features.attentions.data.dto

data class AttentionResponseDto(
    val id: String,
    val reservationId: String,
    val clientId: String,
    val specialistId: String,
    val startedAt: String,
    val finishedAt: String?,
    val durationMinutes: Int?,
    val status: String,
    val observations: String?,
    val createdAt: String
)
