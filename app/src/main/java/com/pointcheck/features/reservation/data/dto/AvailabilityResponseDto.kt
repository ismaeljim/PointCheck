package com.pointcheck.features.reservation.data.dto

/**
 * Objeto de transferencia de datos (DTO) que representa la disponibilidad horaria de un especialista.
 * 
 * @property specialistProfileId Identificador único del especialista consultado.
 * @property date Fecha de la consulta en formato ISO (yyyy-MM-dd).
 * @property availableSlots Lista de horarios disponibles en formato HH:mm.
 */
data class AvailabilityResponseDto(
    val specialistProfileId: String,
    val date: String,
    val availableSlots: List<String>
)
