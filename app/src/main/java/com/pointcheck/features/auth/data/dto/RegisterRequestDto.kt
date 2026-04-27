package com.pointcheck.features.auth.data.dto

/**
 * DTO de red para la solicitud de registro de nuevo usuario.
 * No es una entidad de Room.
 */
data class RegisterRequestDto(
    val name: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    val role: String = "CLIENT"
)
