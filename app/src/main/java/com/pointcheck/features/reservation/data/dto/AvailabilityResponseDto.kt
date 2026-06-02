package com.pointcheck.features.reservation.data.dto

data class AvailabilityResponseDto(
    val specialistId: String,
    val date: String,
    val availableSlots: List<String>
)
