package com.duoc.app.features.user.dto

/**
 * DTO para la solicitud de cambio de contraseña.
 */
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)
