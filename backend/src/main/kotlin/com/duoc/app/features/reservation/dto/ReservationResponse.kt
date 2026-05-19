package com.duoc.app.features.reservation.dto

import com.duoc.app.features.reservation.model.ReservationStatus
import java.time.LocalDateTime

data class ReservationResponse(
    val id: Long,
    val clientId: Long,
    val specialistId: Long,
    val specialistName: String,
    val city: String?,
    val address: String?,
    val serviceId: Long?,
    val serviceName: String?,
    val reservationStart: LocalDateTime,
    val reservationEnd: LocalDateTime?,
    val status: ReservationStatus,
    val notes: String?,
    val createdAt: LocalDateTime
)
