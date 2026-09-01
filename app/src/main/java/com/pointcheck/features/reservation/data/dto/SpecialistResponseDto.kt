package com.pointcheck.features.reservation.data.dto

import com.google.gson.annotations.SerializedName

/**
 * Objeto de transferencia de datos (DTO) que representa a un profesional o especialista disponible para ser reservado.
 * Mapeado desde la entidad professional_profiles del backend.
 * 
 * @property id Identificador único del perfil profesional (UUID).
 * @property userId Identificador único del usuario base asociado.
 * @property name Nombre para mostrar del profesional.
 * @property specialty Descripción de la especialidad o rubro.
 * @property city Ciudad donde presta servicios.
 * @property defaultSessionDurationMinutes Duración estándar de una sesión en minutos.
 * @property latitude Coordenada de latitud de la ubicación del servicio.
 * @property longitude Coordenada de longitud de la ubicación del servicio.
 * @property rut Rol Único Tributario del profesional.
 * @property phone Teléfono de contacto del profesional.
 */
data class SpecialistResponseDto(
    val id: String = "",
    val userId: String = "",
    @SerializedName("displayName")
    val name: String = "Especialista",
    val specialty: String? = null,
    val city: String? = null,
    val defaultSessionDurationMinutes: Int? = 30,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val rut: String? = null,
    val phone: String? = null
)
