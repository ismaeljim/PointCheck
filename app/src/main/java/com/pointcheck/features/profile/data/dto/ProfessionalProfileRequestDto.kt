package com.pointcheck.features.profile.data.dto

/**
 * Objeto de transferencia de datos (DTO) para la creación o actualización de un perfil profesional.
 * 
 * @property userId Identificador del usuario al que pertenece el perfil.
 * @property categoryId Identificador de la categoría de servicios.
 * @property displayName Nombre público del profesional o especialista.
 * @property businessName Nombre comercial o de la empresa (opcional).
 * @property specialty Descripción corta de la especialidad técnica o profesional.
 * @property description Reseña detallada de los servicios y experiencia.
 * @property address Dirección física de atención.
 * @property city Ciudad base de operaciones.
 * @property country País (por defecto "Chile").
 * @property latitude Coordenada geográfica de latitud.
 * @property longitude Coordenada geográfica de longitud.
 * @property defaultSessionDurationMinutes Tiempo estándar de atención en minutos.
 * @property workingHoursJson Configuración de horarios de atención en formato JSON.
 */
data class ProfessionalProfileRequestDto(
    val userId: String,
    val categoryId: String? = null,
    val displayName: String,
    val businessName: String? = null,
    val specialty: String,
    val description: String? = null,
    val address: String? = null,
    val city: String? = null,
    val country: String? = "Chile",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val defaultSessionDurationMinutes: Int = 30,
    val workingHoursJson: String? = null
)
