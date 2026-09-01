package com.pointcheck.features.reservation.data.dto

import com.pointcheck.core.data.dto.UserSummaryDto

/**
 * DTO de Reserva blindado contra nulos para Jetpack Compose.
 * Los campos que se muestran en UI se definen como String no nulo con default "".
 */
data class ReservationResponseDto(
    val id: String = "",
    val client: UserSummaryDto = UserSummaryDto(),
    val specialist: UserSummaryDto = UserSummaryDto(),
    val specialistProfileId: String = "",
    val city: String = "",
    val address: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val categoryIcon: String = "",
    val categoryColor: String = "",
    val isAtHome: Boolean = false,
    val reservationStart: String = "",
    val reservationEnd: String? = null,
    val status: String = "PENDING",
    val notes: String = "",
    val createdAt: String = ""
)
