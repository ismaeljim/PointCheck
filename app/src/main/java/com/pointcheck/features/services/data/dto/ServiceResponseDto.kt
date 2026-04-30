package com.pointcheck.features.services.data.dto

data class ServiceResponseDto(
    val id: Long,
    val professionalProfileId: Long,
    val name: String,
    val description: String? = null,
    val price: Double? = null,
    val durationMinutes: Int? = null,
    val active: Boolean
)
