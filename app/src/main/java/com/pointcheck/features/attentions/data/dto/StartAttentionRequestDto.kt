package com.pointcheck.features.attentions.data.dto

data class StartAttentionRequestDto(
    val reservationId: Long,
    val observations: String? = null
)
