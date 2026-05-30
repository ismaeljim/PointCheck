package com.duoc.app.features.reservation.dto

import com.duoc.app.features.reservation.model.ReservationStatus
import java.time.LocalDateTime

data class ReservationResponse(
    val id: String,
    val clientId: String,
    val clientRut: String?,
    val specialistId: String,
    val specialistName: String,
    val specialistRut: String?,
    val city: String?,
    val address: String?,
    val serviceId: String?,
    val serviceName: String?,
    val categoryIcon: String?,
    val categoryColor: String?,
    val isAtHome: Boolean,
    val reservationStart: LocalDateTime,
    val reservationEnd: LocalDateTime?,
    val status: ReservationStatus,
    val notes: String?,
    val createdAt: LocalDateTime
)
