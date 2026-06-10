package com.duoc.app.features.service.dto

import java.math.BigDecimal

/**
 * DTO que representa un servicio disponible en el catálogo de un especialista.
 *
 * @property id Identificador único del servicio.
 * @property professionalProfileId ID del perfil profesional asociado.
 * @property name Nombre del servicio.
 * @property description Descripción del servicio.
 * @property price Precio configurado.
 * @property durationMinutes Duración de la sesión.
 * @property priceUnit Unidad de medida del precio.
 * @property isAtHome Indica si es servicio a domicilio.
 * @property active Estado de disponibilidad del servicio.
 */
data class ServiceOfferingResponse(
    val id: String?,
    val professionalProfileId: String?,
    val name: String,
    val description: String?,
    val price: BigDecimal?,
    val durationMinutes: Int?,
    val priceUnit: String?,
    val isAtHome: Boolean,
    val active: Boolean
)
