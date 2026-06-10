package com.pointcheck.features.attentions.data.dto

import com.pointcheck.core.data.dto.UserSummaryDto

/**
 * Objeto de transferencia de datos (DTO) que representa el resultado de una atención realizada.
 * Contiene información sobre el tiempo transcurrido, los participantes y las observaciones clínicas o técnicas.
 * 
 * @property id Identificador único de la atención (UUID).
 * @property reservationId Identificador de la reserva asociada a esta atención.
 * @property client Información resumida del cliente atendido.
 * @property specialist Información resumida del especialista que prestó el servicio.
 * @property startedAt Marca de tiempo del inicio de la atención.
 * @property finishedAt Marca de tiempo de la finalización de la atención (nulo si está en curso).
 * @property durationMinutes Duración total calculada de la atención en minutos.
 * @property status Estado actual de la atención (ej: "STARTED", "COMPLETED").
 * @property observations Notas u observaciones registradas durante la atención.
 * @property createdAt Fecha de creación del registro en el sistema.
 */
data class AttentionResponseDto(
    val id: String,
    val reservationId: String,
    val client: UserSummaryDto,
    val specialist: UserSummaryDto,
    val startedAt: String,
    val finishedAt: String?,
    val durationMinutes: Int?,
    val status: String,
    val observations: String?,
    val createdAt: String
)
