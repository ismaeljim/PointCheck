package com.duoc.app.features.professionalprofile.dto

import java.time.LocalDateTime

/**
 * DTO que representa la respuesta con la información completa de un perfil profesional.
 */
data class ProfessionalProfileResponse(
    val id: String,
    val userId: String,
    val categoryId: String?,
    val displayName: String,
    val businessName: String?,
    val specialty: String?,
    val description: String?,
    val address: String?,
    val city: String?,
    val country: String?,
    val defaultSessionDurationMinutes: Int,
    val rating: Float = 0.0f,
    val isVerified: Boolean = false,
    val latitude: Double?,
    val longitude: Double?,
    val workingHoursJson: String?,
    val active: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
)
