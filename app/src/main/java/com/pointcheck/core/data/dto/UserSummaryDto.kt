package com.pointcheck.core.data.dto

/**
 * DTO para representar información resumida de un usuario (Cliente o Especialista).
 * Sincronizado con el backend según el Plan Maestro.
 */
data class UserSummaryDto(
    val id: String,
    val name: String,
    val rut: String,
    val email: String? = null,
    val phone: String? = null,
    val profilePicture: String? = null
)
