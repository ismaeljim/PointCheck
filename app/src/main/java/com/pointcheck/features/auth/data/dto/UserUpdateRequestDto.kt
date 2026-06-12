package com.pointcheck.features.auth.data.dto

/**
 * DTO para la actualización de perfil de usuario en el Frontend.
 */
data class UserUpdateRequestDto(
    val name: String,
    val phone: String,
    val address: String? = null
)
