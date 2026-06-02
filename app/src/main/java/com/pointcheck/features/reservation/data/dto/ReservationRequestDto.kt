package com.pointcheck.features.reservation.data.dto

/**
 * DTO para crear una nueva reserva en el backend.
 */
data class ReservationRequestDto(
    val clientId: String,
    val specialistId: String,
    val serviceId: String? = null,
    val reservationStart: String,
    val reservationEnd: String? = null,
    val notes: String? = null,
    val paymentMethod: String? = null
)
