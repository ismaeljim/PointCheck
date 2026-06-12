package com.pointcheck.features.admin.data.dto

/**
 * DTO para enviar solicitudes de actualización de usuario desde el panel de administración.
 */
data class AdminUserUpdateRequestDto(
    val name: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val role: String? = null,
    val categoryId: String? = null,
    val active: Boolean? = null
)
