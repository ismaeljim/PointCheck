package com.duoc.app.features.reservation.dto

import com.duoc.app.features.reservation.model.ReservationStatus
import java.time.LocalDateTime

data class ReservationResponse(
    val id: Long,
    val clientId: Long,
    val specialistId: Long,
    val serviceId: Long?,
    val reservationStart: LocalDateTime,
    val reservationEnd: LocalDateTime?,
    val status: ReservationStatus,
    val notes: String?,
    val createdAt: LocalDateTime
)
