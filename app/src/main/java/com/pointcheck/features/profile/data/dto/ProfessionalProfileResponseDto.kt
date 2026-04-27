package com.pointcheck.features.profile.data.dto

/**
 * Respuesta del backend con el perfil profesional completo.
 */
data class ProfessionalProfileResponseDto(
    val id: Long,
    val userId: Long,
    val displayName: String,
    val businessName: String? = null,
    val specialty: String,
    val description: String? = null,
    val address: String? = null,
    val city: String? = null,
    val country: String? = "Chile",
    val defaultSessionDurationMinutes: Int = 30,
    val active: Boolean = true
)
