package com.duoc.app.features.user.dto

import com.duoc.app.features.user.model.UserRole

/**
 * DTO que representa la respuesta con la información del usuario tras autenticación o registro.
 */
data class UserResponse(
    val id: String, // UUID estandarizado como String
    val name: String,
    val email: String,
    val rut: String,
    val phone: String,
    val role: UserRole,
    val active: Boolean,
    val categoryId: String? = null // ID de categoría si el usuario es un Especialista
)
