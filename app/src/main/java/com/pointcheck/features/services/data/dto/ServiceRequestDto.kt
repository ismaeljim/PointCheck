package com.pointcheck.features.services.data.dto

/**
 * Objeto de transferencia de datos (DTO) para la creación o actualización de un servicio.
 * 
 * @property professionalProfileId Identificador del perfil profesional que ofrece el servicio.
 * @property name Nombre descriptivo del servicio (ej: "Asesoría Legal").
 * @property description Detalle de lo que incluye el servicio.
 * @property price Costo del servicio.
 * @property durationMinutes Tiempo estimado de duración del servicio.
 */
data class ServiceRequestDto(
    val professionalProfileId: String,
    val name: String,
    val description: String? = null,
    val price: Double,
    val durationMinutes: Int = 30
)
