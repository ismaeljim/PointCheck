package com.pointcheck.features.booking.data.dto

/**
 * Representa un profesional o especialista disponible para reservas.
 * Mapeado desde professional_profiles del backend.
 */
data class SpecialistResponseDto(
    val id: Long,
    val userId: Long,
    val name: String,
    val specialty: String?,
    val experienceYears: Int?,
    val rating: Double = 5.0
)
