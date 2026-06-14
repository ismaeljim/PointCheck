package com.duoc.app.features.reservation.dto

import com.duoc.app.features.reservation.model.ReservationStatus
import com.duoc.app.features.user.dto.UserSummaryDto
import java.time.LocalDateTime

/**
 * DTO que representa la respuesta detallada de una reservación.
 * 
 * Se han forzado tipos String no nulos con valores por defecto para evitar
 * NullPointerExceptions en el renderizado de Jetpack Compose.
 */
data class ReservationResponse(
    val id: String,
    val client: UserSummaryDto,
    val specialist: UserSummaryDto,
    val city: String = "",
    val address: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val categoryIcon: String = "",
    val categoryColor: String = "",
    val isAtHome: Boolean = false,
    val reservationStart: LocalDateTime,
    val reservationEnd: LocalDateTime? = null,
    val status: ReservationStatus,
    val notes: String = "",
    val createdAt: LocalDateTime
)
