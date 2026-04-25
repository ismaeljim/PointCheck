package com.duoc.app.features.professionalprofile.dto

data class ProfessionalProfileRequest(
    val userId: Long,
    val displayName: String,
    val businessName: String? = null,
    val specialty: String? = null,
    val description: String? = null,
    val address: String? = null,
    val city: String? = null,
    val country: String? = null,
    val defaultSessionDurationMinutes: Int = 60
)
