package com.duoc.app.core.exception

import java.time.LocalDateTime

data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String?,
    val timestamp: String = LocalDateTime.now().toString()
)
