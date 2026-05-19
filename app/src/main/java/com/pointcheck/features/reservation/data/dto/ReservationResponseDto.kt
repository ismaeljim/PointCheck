package com.pointcheck.features.reservation.data.dto

/**
 * DTO para recibir la información de una reserva desde el backend.
 */
data class ReservationResponseDto(
    val id: Long,
    val clientId: Long,
    val specialistId: Long,
    val specialistName: String? = null,
    val city: String? = null,
    val address: String? = null,
    val serviceId: Long?,
    val serviceName: String? = null,
    val reservationStart: String,
    val reservationEnd: String?,
    val status: String,
    val notes: String?,
    val createdAt: String
)
