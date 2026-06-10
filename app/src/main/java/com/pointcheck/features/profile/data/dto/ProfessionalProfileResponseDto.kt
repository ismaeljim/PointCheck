package com.pointcheck.features.profile.data.dto

/**
 * Objeto de transferencia de datos (DTO) que representa un perfil profesional completo.
 * 
 * @property id Identificador único del perfil (UUID).
 * @property userId Identificador del usuario base.
 * @property categoryId Identificador de la categoría asignada.
 * @property displayName Nombre público mostrado en la aplicación.
 * @property businessName Razón social o nombre de fantasía.
 * @property specialty Área de especialización.
 * @property description Información bibliográfica y de servicios.
 * @property address Ubicación física registrada.
 * @property city Ciudad de prestación de servicios.
 * @property country País de residencia.
 * @property latitude Latitud para geolocalización.
 * @property longitude Longitud para geolocalización.
 * @property isVerified Indica si el perfil ha sido validado por administración.
 * @property rating Calificación promedio otorgada por clientes.
 * @property workingHoursJson Estructura JSON de los horarios de disponibilidad.
 * @property defaultSessionDurationMinutes Duración base de cada bloque de atención.
 * @property active Estado de visibilidad del perfil en el sistema.
 */
data class ProfessionalProfileResponseDto(
    val id: String,
    val userId: String,
    val categoryId: String? = null,
    val displayName: String? = null,
    val businessName: String? = null,
    val specialty: String? = null,
    val description: String? = null,
    val address: String? = null,
    val city: String? = null,
    val country: String? = "Chile",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isVerified: Boolean = false,
    val rating: Float = 0.0f,
    val workingHoursJson: String? = null,
    val defaultSessionDurationMinutes: Int? = 30,
    val active: Boolean = true
)
