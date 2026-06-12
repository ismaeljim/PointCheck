package com.duoc.app.features.admin.dto

import com.duoc.app.features.user.model.UserRole

/**
 * DTO para la actualización de usuarios desde el panel de administración.
 * 
 * Permite al administrador corregir datos básicos y roles de los usuarios
 * registrados en la plataforma.
 */
data class AdminUserUpdateRequest(
    val name: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val role: UserRole? = null,
    val categoryId: String? = null, // Solo relevante si el usuario es o pasa a ser SPECIALIST
    val active: Boolean? = null
)
