package com.pointcheck.features.reservation.data.dto

/**
 * DTO para crear una nueva reserva en el backend.
 */
data class ReservationRequestDto(
    val clientId: Long,
    val specialistId: Long,
    val serviceId: Long? = null,
    val reservationStart: String,
    val reservationEnd: String? = null,
    val notes: String? = null
)
