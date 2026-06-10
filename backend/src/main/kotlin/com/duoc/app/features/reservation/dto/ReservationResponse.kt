package com.duoc.app.features.reservation.dto

import com.duoc.app.features.reservation.model.ReservationStatus
import com.duoc.app.features.user.dto.UserSummaryDto
import java.time.LocalDateTime

/**
 * DTO que representa la respuesta detallada de una reservación.
 *
 * Incluye información extendida del servicio, ubicación y estado para su
 * visualización en la aplicación móvil.
 *
 * @property id Identificador único de la reserva.
 * @property client Resumen del cliente.
 * @property specialist Resumen del especialista.
 * @property city Ciudad donde se realizará el servicio.
 * @property address Dirección física de la cita.
 * @property serviceId ID del servicio prestado.
 * @property serviceName Nombre descriptivo del servicio.
 * @property categoryIcon Clave del icono de la categoría para UI.
 * @property categoryColor Color hexadecimal de la categoría para UI.
 * @property isAtHome Indica si el servicio es a domicilio.
 * @property reservationStart Fecha y hora de inicio.
 * @property reservationEnd Fecha y hora de término estimada.
 * @property status Estado actual de la reserva (PENDING, CONFIRMED, etc.).
 * @property notes Notas adjuntas a la reserva.
 * @property createdAt Fecha de creación del registro.
 */
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
