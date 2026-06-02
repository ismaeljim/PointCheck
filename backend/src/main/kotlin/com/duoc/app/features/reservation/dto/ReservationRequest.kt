package com.duoc.app.features.reservation.dto

import com.duoc.app.features.billing.model.PaymentMethod
import java.time.LocalDateTime

data class ReservationRequest(
    val clientId: String,
    val specialistId: String,
    val serviceId: String? = null,
    val reservationStart: LocalDateTime,
    val reservationEnd: LocalDateTime? = null,
    val notes: String? = null,
    val paymentMethod: PaymentMethod? = null
)
