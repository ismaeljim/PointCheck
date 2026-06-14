package com.duoc.app.features.dashboard.dto

import com.duoc.app.features.reservation.dto.ReservationResponse
import java.time.LocalDateTime

data class ClientDashboardResponse(
    val nextAppointment: ReservationResponse? = null,
    val favoriteSpecialists: List<FavoriteSpecialistDto> = emptyList(),
    val recentNotifications: List<NotificationSummaryDto> = emptyList()
)

data class FavoriteSpecialistDto(
    val specialistId: String,
    val name: String,
    val specialty: String = "",
    val visitCount: Long
)

data class NotificationSummaryDto(
    val id: String,
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean,
    val createdAt: LocalDateTime
)
