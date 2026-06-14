package com.duoc.app.features.user.dto

import com.duoc.app.features.user.model.UserRole

/**
 * DTO que representa la respuesta con la información del usuario tras autenticación o registro.
 */
data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    val rut: String,
    val phone: String,
    val address: String? = null,
    val role: UserRole,
    val active: Boolean,
    val categoryId: String? = null,
    val token: String? = null
)
