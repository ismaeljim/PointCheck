package com.pointcheck.features.profile.data.dto

/**
 * Respuesta del backend con el perfil profesional completo.
 */
data class ProfessionalProfileResponseDto(
<<<<<<< Updated upstream
    val id: Long,
    val userId: Long,
    val categoryId: Long? = null,
    val displayName: String? = null,
=======
    val id: String,
    val userId: String,
    val displayName: String,
>>>>>>> Stashed changes
    val businessName: String? = null,
    val specialty: String? = null,
    val description: String? = null,
    val address: String? = null,
    val city: String? = null,
    val country: String? = "Chile",
    val defaultSessionDurationMinutes: Int? = 30,
    val active: Boolean = true
)
