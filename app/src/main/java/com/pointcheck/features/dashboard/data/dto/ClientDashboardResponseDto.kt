package com.pointcheck.features.dashboard.data.dto

import com.pointcheck.features.reservation.data.dto.ReservationResponseDto

data class ClientDashboardResponseDto(
    val nextAppointment: ReservationResponseDto? = null,
    val favoriteSpecialists: List<FavoriteSpecialistDto>? = emptyList(),
    val recentNotifications: List<NotificationSummaryDto>? = emptyList()
)

data class FavoriteSpecialistDto(
    val specialistProfileId: String = "",
    val name: String = "",
    val specialty: String? = null,
    val visitCount: Long = 0
)

data class NotificationSummaryDto(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "INFO",
    val isRead: Boolean = true,
    val createdAt: String = ""
)
