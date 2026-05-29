package com.pointcheck.features.auth.data.dto

/**
 * DTO de red para la solicitud de registro de nuevo usuario.
 * No es una entidad de Room.
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
    val categoryId: Long? = null,
    val services: List<ServiceOfferingDto>? = null
)

data class ServiceOfferingDto(
    val templateId: Long,
    val price: Double,
    val unit: String
)
