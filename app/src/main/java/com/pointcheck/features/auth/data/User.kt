package com.pointcheck.features.auth.data

/**
 * Modelo de datos simplificado para el Usuario en el lado del cliente.
 * Se utiliza principalmente para mantener el estado de la sesión local.
 */
data class User(
    val email: String,
    val name: String,
    val password: String = ""
)

