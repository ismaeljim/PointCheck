package com.duoc.app.features.reservation.dto

import com.duoc.app.features.billing.model.PaymentMethod
import java.time.LocalDateTime

/**
 * DTO para la solicitud de creación de una reservación.
 *
 * @property clientId ID del usuario que solicita el servicio.
 * @property specialistProfileId ID del perfil profesional que realizará el servicio.
 * @property serviceId ID del servicio específico del catálogo (opcional).
 * @property reservationStart Fecha y hora de inicio de la cita.
 * @property reservationEnd Fecha y hora de término (opcional, calculada según el servicio si no se provee).
 * @property notes Observaciones adicionales del cliente.
 * @property paymentMethod Método de pago preferido.
 */
data class ReservationRequest(
    val clientId: String,
    val specialistProfileId: String,
    val serviceId: String? = null,
    val reservationStart: LocalDateTime,
    val reservationEnd: LocalDateTime? = null,
    val notes: String? = null,
    val paymentMethod: PaymentMethod? = null
)
