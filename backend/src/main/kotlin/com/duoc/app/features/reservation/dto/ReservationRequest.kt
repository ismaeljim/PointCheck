package com.duoc.app.features.reservation.dto

import java.time.LocalDateTime

data class ReservationRequest(
    val clientId: String,
    val specialistId: String,
    val serviceId: String? = null,
    val reservationStart: LocalDateTime,
    val reservationEnd: LocalDateTime? = null,
    val notes: String? = null
)
