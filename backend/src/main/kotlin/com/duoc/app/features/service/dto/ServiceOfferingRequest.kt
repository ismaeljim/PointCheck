package com.duoc.app.features.service.dto

import com.duoc.app.features.service.model.PriceUnit
import java.math.BigDecimal

/**
 * DTO para la creación o actualización de un servicio en el catálogo.
 *
 * @property professionalProfileId ID del perfil profesional dueño del servicio.
 * @property name Nombre comercial del servicio.
 * @property description Descripción detallada de la prestación.
 * @property price Precio base del servicio.
 * @property durationMinutes Tiempo estimado de la atención en minutos.
 * @property priceUnit Unidad de cobro (por sesión, por hora, etc.).
 * @property isAtHome Define si el servicio se presta en el domicilio del cliente.
 */
data class ServiceOfferingRequest(
    val professionalProfileId: String,
    val name: String,
    val description: String? = null,
    val price: BigDecimal? = null,
    val durationMinutes: Int? = null,
    val priceUnit: PriceUnit = PriceUnit.SESSION,
    val isAtHome: Boolean = false
)
