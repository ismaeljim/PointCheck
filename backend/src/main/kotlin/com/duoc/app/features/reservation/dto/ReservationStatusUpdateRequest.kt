package com.duoc.app.features.reservation.dto

import com.duoc.app.features.reservation.model.ReservationStatus

data class ReservationStatusUpdateRequest(
    val status: ReservationStatus
)
