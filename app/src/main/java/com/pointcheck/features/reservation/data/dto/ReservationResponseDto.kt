package com.pointcheck.features.reservation.data.dto

import com.pointcheck.core.data.dto.UserSummaryDto

/**
 * Objeto de transferencia de datos (DTO) que representa una reserva de servicio en el sistema.
 * Contiene información detallada sobre los participantes, el servicio, la ubicación y el horario pactado.
 * 
 * @property id Identificador único de la reserva (UUID).
 * @property client Información resumida del cliente que realizó la reserva.
 * @property specialist Información resumida del especialista que prestará el servicio.
 * @property city Ciudad donde se llevará a cabo el servicio.
 * @property address Dirección específica del servicio.
 * @property serviceId Identificador del servicio contratado.
 * @property serviceName Nombre descriptivo del servicio.
 * @property categoryIcon Nombre del icono representativo de la categoría del servicio.
 * @property categoryColor Código de color hexadecimal para la UI de la categoría.
 * @property isAtHome Indica si el servicio es a domicilio (true) o en el local del profesional (false).
 * @property reservationStart Fecha y hora de inicio programada.
 * @property reservationEnd Fecha y hora de término estimada.
 * @property status Estado actual de la reserva (ej: "PENDING", "CONFIRMED", "CANCELLED", "COMPLETED").
 * @property notes Instrucciones adicionales proporcionadas por el cliente.
 * @property createdAt Fecha de creación de la reserva.
 */
data class ReservationResponseDto(
    val id: String,
    val client: UserSummaryDto,
    val specialist: UserSummaryDto,
    val city: String? = null,
    val address: String? = null,
    val serviceId: String?,
    val serviceName: String? = null,
    val categoryIcon: String? = null,
    val categoryColor: String? = null,
    val isAtHome: Boolean = false,
    val reservationStart: String,
    val reservationEnd: String?,
    val status: String,
    val notes: String?,
    val createdAt: String
)
