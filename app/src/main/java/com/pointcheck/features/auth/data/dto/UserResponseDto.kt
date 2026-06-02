package com.pointcheck.features.auth.data.dto

/**
 * DTO de respuesta para la información del usuario tras autenticación exitosa.
 * Contiene los datos básicos de perfil y el rol asignado.
 */
data class UserResponseDto(
    val id: String, // UUID del usuario
    val name: String,
    val email: String,
    val rut: String,
    val phone: String,
    val role: String, // CLIENT, SPECIALIST o ADMIN
    val active: Boolean,
    val categoryId: String? = null // Presente solo si el rol es SPECIALIST
)
