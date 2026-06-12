package com.duoc.app.features.user.dto

/**
 * DTO para la actualización de perfil de usuario (Autogestión).
 */
data class UserUpdateRequest(
    val name: String,
    val phone: String,
    val address: String? = null
)
