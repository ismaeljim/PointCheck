package com.pointcheck.features.auth.data.dto

/**
 * DTO para la solicitud de cambio de contraseña en la App.
 */
data class ChangePasswordRequestDto(
    val currentPassword: String,
    val newPassword: String
)
