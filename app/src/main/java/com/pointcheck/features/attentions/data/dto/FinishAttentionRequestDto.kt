package com.pointcheck.features.attentions.data.dto

/**
 * DTO para la solicitud de finalización de una atención.
 * 
 * @property observations Notas finales o resultados de la atención prestada.
 * @property durationMinutes Duración manual de la atención si difiere del tiempo automático.
 */
data class FinishAttentionRequestDto(
    val observations: String? = null,
    val durationMinutes: Int? = null
)
