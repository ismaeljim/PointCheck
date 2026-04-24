package com.duoc.app.core.exception

import java.time.LocalDateTime

data class ApiErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
