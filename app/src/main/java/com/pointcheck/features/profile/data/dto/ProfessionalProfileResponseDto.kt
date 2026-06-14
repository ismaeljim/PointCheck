package com.pointcheck.features.profile.data.dto

/**
 * DTO de Perfil Profesional blindado contra nulos.
 * Campos como address, specialty y description son ahora String no nulos.
 */
data class ProfessionalProfileResponseDto(
    val id: String,
    val userId: String,
    val categoryId: String = "",
    val displayName: String = "",
    val businessName: String = "",
    val specialty: String = "",
    val description: String = "",
    val address: String = "",
    val city: String = "",
    val country: String = "Chile",
    val latitude: Double? = null, // Las coordenadas pueden ser null si no se han seteado
    val longitude: Double? = null,
    val isVerified: Boolean = false,
    val rating: Float = 0.0f,
    val workingHoursJson: String = "",
    val defaultSessionDurationMinutes: Int = 30,
    val active: Boolean = true
)
