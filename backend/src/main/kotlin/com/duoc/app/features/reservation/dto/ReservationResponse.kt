package com.duoc.app.features.reservation.dto

import com.duoc.app.features.reservation.model.ReservationStatus
import com.duoc.app.features.user.dto.UserSummaryDto
import java.time.LocalDateTime

data class ReservationResponse(
    val id: String,
    val client: UserSummaryDto,
    val specialist: UserSummaryDto,
    val city: String?,
    val address: String?,
    val serviceId: String?,
    val serviceName: String?,
    val categoryIcon: String?,
    val categoryColor: String?,
    val isAtHome: Boolean,
    val reservationStart: LocalDateTime,
    val reservationEnd: LocalDateTime?,
    val status: ReservationStatus,
    val notes: String?,
    val createdAt: LocalDateTime
)
