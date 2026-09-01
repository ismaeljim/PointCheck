package com.duoc.app.features.auth.dto

import com.duoc.app.features.user.model.UserRole
import java.math.BigDecimal

/**
 * DTO para la solicitud de registro de nuevos usuarios.
 * Soporta registro de Clientes y Especialistas (con datos de perfil opcionales).
 */
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val rut: String,
    val phone: String,
    val role: UserRole = UserRole.CLIENT,
    // Campos opcionales para rol SPECIALIST
    val city: String? = null,
    val address: String? = null,
    val categoryId: String? = null,
    val services: List<ServiceOfferingRegisterRequest>? = null
)

/**
 * Representación de un servicio ofrecido durante el registro inicial.
 */
data class ServiceOfferingRegisterRequest(
    val templateId: String,
    val price: BigDecimal,
    val unit: String
)
