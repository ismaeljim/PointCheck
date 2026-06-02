package com.duoc.app.features.professionalprofile.dto

/**
 * DTO para la creación o actualización de un perfil profesional.
 */
data class ProfessionalProfileRequest(
    val userId: String,
    val categoryId: String? = null,
    val displayName: String,
    val businessName: String? = null,
    val specialty: String? = null,
    val description: String? = null,
    val address: String? = null,
    val city: String? = null,
    val country: String? = "Chile",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val defaultSessionDurationMinutes: Int = 60,
    val workingHoursJson: String? = null
)
