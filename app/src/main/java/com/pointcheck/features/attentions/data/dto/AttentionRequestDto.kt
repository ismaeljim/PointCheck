package com.pointcheck.features.attentions.data.dto

data class AttentionRequestDto(
    val reservationId: Long,
    val clientId: Long,
    val specialistId: Long,
    val observations: String? = null
)
