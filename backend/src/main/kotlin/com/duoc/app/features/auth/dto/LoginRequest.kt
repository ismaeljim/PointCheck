package com.duoc.app.features.auth.dto

/**
 * DTO para la solicitud de inicio de sesión.
 */
data class LoginRequest(
    val email: String,
    val password: String
)
