package com.pointcheck.features.reservation.data.dto

import com.pointcheck.core.data.dto.UserSummaryDto

/**
 * DTO para recibir la información de una reserva desde el backend.
 * Actualizado para usar UserSummaryDto.
 */
data class ReservationResponseDto(
    val id: String,
    val client: UserSummaryDto,
    val specialist: UserSummaryDto,
    val city: String? = null,
    val address: String? = null,
    val serviceId: String?,
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
