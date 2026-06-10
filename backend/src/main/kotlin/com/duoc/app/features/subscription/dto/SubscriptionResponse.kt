package com.duoc.app.features.subscription.dto

import com.duoc.app.features.subscription.model.SubscriptionStatus
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * DTO que representa el estado de una suscripción activa o histórica.
 *
 * @property id Identificador de la suscripción.
 * @property professionalProfileId ID del perfil profesional asociado.
 * @property planName Nombre del plan contratado.
 * @property status Estado de la suscripción (ACTIVE, EXPIRED, CANCELLED).
 * @property startDate Inicio de vigencia.
 * @property endDate Término de vigencia.
 * @property createdAt Fecha de contratación.
 * @property updatedAt Última actualización de estado.
 */
data class SubscriptionResponse(
    val id: String?,
    val professionalProfileId: String?,
    val planName: String,
    val status: SubscriptionStatus,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
)
