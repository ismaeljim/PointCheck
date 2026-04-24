package com.duoc.app.features.attention.dto

data class StartAttentionRequest(
    val reservationId: Long,
    val observations: String? = null
)
