package com.pointcheck.features.profile.data.dto

/**
 * Respuesta del backend con el perfil profesional completo.
 */
data class ProfessionalProfileResponseDto(
    val id: String,
    val userId: String,
    val categoryId: String? = null,
    val displayName: String? = null,
    val businessName: String? = null,
    val specialty: String? = null,
    val description: String? = null,
    val address: String? = null,
    val city: String? = null,
    val country: String? = "Chile",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isVerified: Boolean = false,
    val rating: Float = 0.0f,
    val workingHoursJson: String? = null,
    val defaultSessionDurationMinutes: Int? = 30,
    val active: Boolean = true
)
