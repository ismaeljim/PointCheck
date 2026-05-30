package com.pointcheck.features.profile.data.dto

/**
 * DTO para crear o actualizar un perfil profesional.
 */
data class ProfessionalProfileRequestDto(
    val userId: String,
    val categoryId: String? = null,
    val displayName: String,
    val businessName: String? = null,
    val specialty: String,
    val description: String? = null,
    val address: String? = null,
    val city: String? = null,
    val country: String? = "Chile",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val defaultSessionDurationMinutes: Int = 30
)
