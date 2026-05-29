package com.duoc.app.features.professionalprofile.dto

data class ProfessionalProfileRequest(
<<<<<<< Updated upstream
    val userId: Long,
    val categoryId: Long? = null,
=======
    val userId: String,
>>>>>>> Stashed changes
    val displayName: String,
    val businessName: String? = null,
    val specialty: String? = null,
    val description: String? = null,
    val address: String? = null,
    val city: String? = null,
    val country: String? = null,
    val defaultSessionDurationMinutes: Int = 60
)
