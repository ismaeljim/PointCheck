package com.pointcheck.features.attentions.data.dto

/**
 * DTO para la solicitud de inicio de una atención.
 * 
 * @property reservationId Identificador de la reserva que se va a iniciar.
 * @property observations Notas iniciales opcionales antes de comenzar el servicio.
 */
data class StartAttentionRequestDto(
    val reservationId: String,
    val observations: String? = null
)
