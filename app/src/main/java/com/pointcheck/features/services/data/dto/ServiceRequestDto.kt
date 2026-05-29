package com.pointcheck.features.services.data.dto

data class ServiceRequestDto(
    val professionalProfileId: String,
    val name: String,
    val description: String? = null,
    val price: Double,
    val durationMinutes: Int = 30
)
