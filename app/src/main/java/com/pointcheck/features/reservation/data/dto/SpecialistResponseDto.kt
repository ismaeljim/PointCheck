package com.pointcheck.features.reservation.data.dto

import com.google.gson.annotations.SerializedName

/**
 * Representa un profesional o especialista disponible para reservas.
 * Mapeado desde professional_profiles del backend.
 */
data class SpecialistResponseDto(
    val id: Long,        // Profile ID
    val userId: Long,    // User ID
    @SerializedName("displayName")
    val name: String,
    val specialty: String?,
    val defaultSessionDurationMinutes: Int? = 30
)
