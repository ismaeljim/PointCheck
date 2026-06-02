package com.pointcheck.features.attentions.data.dto

data class StartAttentionRequestDto(
    val reservationId: String,
    val observations: String? = null
)
