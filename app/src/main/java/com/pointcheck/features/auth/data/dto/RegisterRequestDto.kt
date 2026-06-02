package com.pointcheck.features.auth.data.dto

/**
 * DTO de red para la solicitud de registro de nuevo usuario.
 * No es una entidad de Room.
 */
/**
 * DTO para la solicitud de registro enviada al servidor.
 * Encapsula datos de usuario y perfil profesional si aplica.
 */
data class RegisterRequestDto(
    val name: String,
    val email: String,
    val password: String,
    val rut: String,
    val phone: String,
    val role: String = "CLIENT",
    val city: String? = null,
    val address: String? = null,
    val categoryId: String? = null,
    // Lista de servicios iniciales si el usuario es un especialista
    val services: List<ServiceOfferingDto>? = null
)

/**
 * Representación de un servicio ofrecido durante el registro inicial.
 */
data class ServiceOfferingDto(
    val templateId: String,
    val price: Double,
    val unit: String
)
