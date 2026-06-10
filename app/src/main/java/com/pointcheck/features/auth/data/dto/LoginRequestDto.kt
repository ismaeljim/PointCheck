package com.pointcheck.features.auth.data.dto

/**
 * Objeto de transferencia de datos (DTO) para la solicitud de inicio de sesión.
 * Contiene las credenciales necesarias para autenticar a un usuario en el sistema.
 * 
 * @property email Correo electrónico registrado del usuario.
 * @property password Contraseña en texto plano (será transmitida de forma segura sobre HTTPS).
 */
data class LoginRequestDto(
    val email: String,
    val password: String
)
