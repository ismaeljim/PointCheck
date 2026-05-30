package com.duoc.app.features.reservation.dto

import java.time.LocalDate
import java.time.LocalTime

data class AvailabilityResponse(
    val specialistId: String,
    val date: LocalDate,
    val availableSlots: List<LocalTime>
)
