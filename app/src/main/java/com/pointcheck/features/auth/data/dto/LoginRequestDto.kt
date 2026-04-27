package com.pointcheck.features.auth.data.dto

/**
 * DTO de red para la solicitud de inicio de sesión.
 * No es una entidad de Room.
 */
data class LoginRequestDto(
    val email: String,
    val password: String
)
