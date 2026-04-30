package com.pointcheck.features.booking.data.dto

/**
 * DTO para crear una nueva reserva en el backend.
 */
data class ReservationRequestDto(
    val clientId: Long,
    val specialistId: Long,
    val serviceId: Long,
    val reservationStart: String, // Formato ISO: "yyyy-MM-dd'T'HH:mm:ss"
    val notes: String? = null
)
