package com.pointcheck.features.dashboard.data.dto

import com.pointcheck.features.reservation.data.dto.ReservationResponseDto

data class ClientDashboardResponseDto(
    val nextAppointment: ReservationResponseDto? = null,
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
    val createdAt: String
)
