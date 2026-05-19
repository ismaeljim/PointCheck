package com.duoc.app.features.dashboard.dto

import com.duoc.app.features.reservation.dto.ReservationResponse
import com.duoc.app.features.notification.model.Notification
import java.time.LocalDateTime

data class ClientDashboardResponse(
    val nextAppointment: ReservationResponse? = null,
    val favoriteSpecialists: List<FavoriteSpecialistDto> = emptyList(),
    val recentNotifications: List<NotificationSummaryDto> = emptyList()
)

data class FavoriteSpecialistDto(
    val specialistId: Long,
    val name: String,
    val specialty: String?,
    val visitCount: Long
)

data class NotificationSummaryDto(
    val id: Long,
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean,
    val createdAt: LocalDateTime
)
