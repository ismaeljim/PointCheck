package com.pointcheck.features.reservation.data.dto

/**
 * DTO para recibir la información de una reserva desde el backend.
 */
data class ReservationResponseDto(
    val id: Long,
    val clientId: Long,
    val clientRut: String? = null,
    val specialistId: Long,
    val specialistName: String? = null,
    val specialistRut: String? = null,
    val city: String? = null,
    val address: String? = null,
    val serviceId: Long?,
    val serviceName: String? = null,
    val categoryIcon: String? = null,
    val categoryColor: String? = null,
    val isAtHome: Boolean = false,
    val reservationStart: String,
    val reservationEnd: String?,
    val status: String,
    val notes: String?,
    val createdAt: String
)
