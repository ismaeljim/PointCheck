package com.duoc.app.features.reservation.dto

import com.duoc.app.features.reservation.model.ReservationStatus
import java.time.LocalDateTime

data class ReservationResponse(
    val id: Long,
    val clientId: Long,
    val clientRut: String?,
    val specialistId: Long,
    val specialistName: String,
    val specialistRut: String?,
    val city: String?,
    val address: String?,
    val serviceId: Long?,
    val serviceName: String?,
    val categoryIcon: String?,
    val categoryColor: String?,
    val isAtHome: Boolean,
    val reservationStart: java.time.LocalDateTime,
    val reservationEnd: java.time.LocalDateTime?,
    val status: com.duoc.app.features.reservation.model.ReservationStatus,
    val notes: String?,
    val createdAt: java.time.LocalDateTime
)
