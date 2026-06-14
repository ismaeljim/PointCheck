package com.duoc.app.features.admin.dto

/**
 * DTO para la visualización de usuarios en el panel de administración.
 * Desacopla la entidad User de la respuesta de la API.
 */
data class AdminUserResponse(
    val id: String,
    val name: String,
    val email: String,
    val rut: String,
    val role: String,
    val active: Boolean,
    val createdAt: String,
    val phone: String,
    val address: String = ""
)
