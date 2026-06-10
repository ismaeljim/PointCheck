package com.pointcheck.features.reservation.data.dto

/**
 * Objeto de transferencia de datos (DTO) para actualizar el estado de una reserva.
 * Utilizado para transiciones de estado como de 'PENDING' a 'CONFIRMED' o 'CANCELLED'.
 * 
 * @property status El nuevo estado que se desea asignar a la reserva.
 */
data class ReservationStatusUpdateRequestDto(
    val status: String
)
