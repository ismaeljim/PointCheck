package com.pointcheck.features.subscriptions.data.dto

/**
 * Objeto de transferencia de datos (DTO) para la creación de una suscripción.
 * Contiene la información mínima requerida para registrar una nueva suscripción en el sistema.
 *
 * @property professionalProfileId ID del perfil profesional al que se le asignará la suscripción.
 * @property planName Nombre del plan seleccionado (ej: "BASIC", "PREMIUM").
 * @property startDate Fecha de inicio de la suscripción en formato ISO (yyyy-MM-dd).
 * @property endDate Fecha de finalización de la suscripción en formato ISO (yyyy-MM-dd).
 */
data class SubscriptionRequestDto(
    val professionalProfileId: String,
    val planName: String,
    val startDate: String,
    val endDate: String
)
