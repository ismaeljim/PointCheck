package com.pointcheck.features.auth.data.dto

/**
 * Objeto de transferencia de datos (DTO) para la solicitud de registro de un nuevo usuario.
 * Soporta tanto el registro de clientes básicos como de especialistas con perfil profesional inicial.
 * 
 * @property name Nombre completo del usuario.
 * @property email Correo electrónico único para la cuenta.
 * @property password Contraseña elegida por el usuario.
 * @property rut Rol Único Tributario (RUT) del usuario.
 * @property phone Teléfono de contacto.
 * @property role Rol del usuario (ej: "CLIENT", "SPECIALIST").
 * @property city Ciudad de residencia o de prestación de servicios.
 * @property address Dirección física (requerida para especialistas).
 * @property categoryId ID de la categoría profesional (solo para especialistas).
 * @property services Lista de servicios ofrecidos inicialmente por el especialista.
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
    val services: List<ServiceOfferingDto>? = null
)

/**
 * Representación de un servicio ofrecido durante el registro inicial del especialista.
 * 
 * @property templateId ID de la plantilla de servicio de la categoría seleccionada.
 * @property price Precio base definido por el especialista para este servicio.
 * @property unit Unidad de cobro (ej: "POR_HORA", "POR_SESION").
 */
data class ServiceOfferingDto(
    val templateId: String,
    val price: Double,
    val unit: String
)
