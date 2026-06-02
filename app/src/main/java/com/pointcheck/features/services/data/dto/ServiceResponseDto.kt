package com.pointcheck.features.services.data.dto

data class ServiceResponseDto(
    val id: String,
    val professionalProfileId: String,
    val name: String,
    val description: String? = null,
    val price: Double? = null,
    val durationMinutes: Int? = null,
    val priceUnit: String? = "SESSION",
    val active: Boolean
)
