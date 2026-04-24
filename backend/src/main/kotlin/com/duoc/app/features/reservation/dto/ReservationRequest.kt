package com.duoc.app.features.reservation.dto

import java.time.LocalDateTime

data class ReservationRequest(
    val clientId: Long,
    val specialistId: Long,
    val serviceId: Long? = null,
    val reservationStart: LocalDateTime,
    val reservationEnd: LocalDateTime? = null,
    val notes: String? = null
)
