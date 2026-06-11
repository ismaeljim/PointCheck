package com.pointcheck.features.auth.data.dto

/**
 * Objeto de transferencia de datos (DTO) para la respuesta de información de usuario.
 * Se recibe tras un inicio de sesión exitoso o al consultar perfiles.
 * 
 * @property id Identificador único del usuario (UUID en formato String).
 * @property name Nombre completo del usuario.
 * @property email Correo electrónico asociado a la cuenta.
 * @property rut Rol Único Tributario (RUT).
 * @property phone Teléfono de contacto.
 * @property role Rol asignado en el sistema (CLIENT, SPECIALIST, ADMIN).
 * @property active Indica si la cuenta del usuario está habilitada.
 * @property categoryId Identificador de la categoría profesional (solo si es SPECIALIST).
 */
data class UserResponseDto(
    val id: String,
    val name: String,
    val email: String,
    val rut: String,
    val phone: String,
    val role: String,
    val active: Boolean,
    val address: String? = null,
    val categoryId: String? = null
)
