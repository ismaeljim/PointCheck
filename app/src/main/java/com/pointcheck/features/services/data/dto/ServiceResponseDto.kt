package com.pointcheck.features.services.data.dto

/**
 * Objeto de transferencia de datos (DTO) que representa un servicio ofrecido por un profesional.
 * 
 * @property id Identificador único del servicio (UUID).
 * @property professionalProfileId Identificador del perfil profesional asociado.
 * @property name Nombre del servicio.
 * @property description Descripción detallada.
 * @property price Valor monetario del servicio.
 * @property durationMinutes Duración del servicio en minutos.
 * @property priceUnit Unidad de cobro (ej: "SESSION", "HOUR", "DAY").
 * @property isAtHome Indica si el servicio se presta a domicilio.
 * @property active Estado de disponibilidad del servicio.
 */
data class ServiceResponseDto(
    val id: String = "",
    val professionalProfileId: String = "",
    val name: String = "Servicio sin nombre",
    val description: String? = "",
    val price: Double? = 0.0,
    val durationMinutes: Int? = 0,
    val priceUnit: String? = "SESSION",
    val isAtHome: Boolean = false,
    val active: Boolean = true
)
