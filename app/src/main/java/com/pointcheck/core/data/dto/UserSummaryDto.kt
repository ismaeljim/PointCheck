package com.pointcheck.core.data.dto

/**
 * Objeto de transferencia de datos (DTO) para representar información resumida de un usuario.
 * Utilizado para mostrar perfiles rápidos de Clientes o Especialistas en listas y detalles básicos.
 * 
 * @property id Identificador único del usuario (UUID en formato String).
 * @property name Nombre completo del usuario.
 * @property rut Rol Único Tributario (RUT) formateado.
 * @property email Dirección de correo electrónico opcional.
 * @property phone Número de contacto telefónico opcional.
 * @property profilePicture URL o ruta de la imagen de perfil del usuario.
 */
data class UserSummaryDto(
    val id: String,
    val name: String,
    val rut: String,
    val email: String = "",
    val phone: String = "",
    val profilePicture: String = ""
)
