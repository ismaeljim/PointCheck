package com.duoc.app.features.attention.dto

data class StartAttentionRequest(
    val reservationId: String,
    val observations: String? = null
)
