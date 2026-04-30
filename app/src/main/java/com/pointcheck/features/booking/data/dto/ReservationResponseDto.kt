package com.pointcheck.features.booking.data.dto

/**
 * DTO para recibir la información de una reserva desde el backend.
 */
data class ReservationResponseDto(
    val id: Long,
    val clientId: Long,
    val specialistId: Long,
    val serviceId: Long,
    val reservationStart: String,
    val status: String,
    val serviceName: String? = null,
    val specialistName: String? = null,
    val price: Double? = null
)
