package com.pointcheck.features.auth.data.dto

/**
 * DTO para la respuesta de usuario.
 * Incluye el token JWT necesario para autenticar peticiones posteriores.
 */
data class UserResponseDto(
    val id: String,
    val token: String? = null, // Llave de acceso JWT
    val name: String,
    val email: String,
    val rut: String,
    val phone: String,
    val role: String,
    val active: Boolean,
    val address: String? = null,
    val categoryId: String? = null
)
