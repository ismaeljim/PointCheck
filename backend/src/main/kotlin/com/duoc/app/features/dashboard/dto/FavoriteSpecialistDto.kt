package com.duoc.app.features.dashboard.dto

data class FavoriteSpecialistDto(
    val specialistProfileId: String,
    val name: String,
    val specialty: String = "",
    val visitCount: Long
)
