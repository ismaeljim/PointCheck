package com.pointcheck.features.auth.data.dto

/**
 * DTO de red para la respuesta de información de usuario desde el backend.
 */
data class UserResponseDto(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String?,
    val role: String,
    val active: Boolean
)
