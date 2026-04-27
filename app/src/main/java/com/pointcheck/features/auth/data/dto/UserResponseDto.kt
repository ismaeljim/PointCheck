package com.pointcheck.features.auth.data.dto

/**
 * DTO de red para la respuesta de información de usuario desde el backend.
 * No es una entidad de Room.
 */
data class UserResponseDto(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String? = null,
    val role: String,
    val active: Boolean = true
)
