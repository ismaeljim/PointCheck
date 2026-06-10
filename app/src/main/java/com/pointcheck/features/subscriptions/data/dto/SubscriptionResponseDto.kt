package com.pointcheck.features.subscriptions.data.dto

/**
 * Objeto de transferencia de datos (DTO) que representa la respuesta de una suscripción.
 * Contiene la información detallada de una suscripción devuelta por el servidor.
 *
 * @property id Identificador único de la suscripción.
 * @property professionalProfileId ID del perfil profesional asociado.
 * @property planName Nombre del plan de suscripción (ej: "BASIC", "PREMIUM").
 * @property status Estado actual de la suscripción (ej: "ACTIVE", "CANCELLED", "EXPIRED").
 * @property startDate Fecha de inicio de la suscripción.
 * @property endDate Fecha de finalización de la suscripción.
 * @property createdAt Marca de tiempo de creación del registro.
 * @property updatedAt Marca de tiempo de la última actualización, puede ser nulo.
 */
data class SubscriptionResponseDto(
    val id: String,
    val professionalProfileId: String,
    val planName: String,
    val status: String,
    val startDate: String,
    val endDate: String,
    val createdAt: String,
    val updatedAt: String?
)
