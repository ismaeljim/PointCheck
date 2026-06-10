package com.duoc.app.features.subscription.dto

import java.time.LocalDate

/**
 * DTO para la solicitud de una nueva suscripción.
 *
 * @property professionalProfileId ID del perfil profesional que suscribe.
 * @property planName Nombre del plan (ej: "Mensual", "Anual").
 * @property startDate Fecha de inicio de vigencia.
 * @property endDate Fecha de término de vigencia.
 */
data class SubscriptionRequest(
    val professionalProfileId: String,
    val planName: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)
