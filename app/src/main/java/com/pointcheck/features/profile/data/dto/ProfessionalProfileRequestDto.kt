package com.pointcheck.features.profile.data.dto

/**
 * DTO para crear o actualizar un perfil profesional.
 */
data class ProfessionalProfileRequestDto(
<<<<<<< Updated upstream
    val userId: Long,
    val categoryId: Long? = null,
=======
    val userId: String,
>>>>>>> Stashed changes
    val displayName: String,
    val businessName: String? = null,
    val specialty: String,
    val description: String? = null,
    val address: String? = null,
    val city: String? = null,
    val country: String? = "Chile",
    val defaultSessionDurationMinutes: Int = 30
)
