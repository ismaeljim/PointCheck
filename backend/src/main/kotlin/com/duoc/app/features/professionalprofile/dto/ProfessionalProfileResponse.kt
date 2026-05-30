package com.duoc.app.features.professionalprofile.dto

import java.time.LocalDateTime

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
    val active: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
)
