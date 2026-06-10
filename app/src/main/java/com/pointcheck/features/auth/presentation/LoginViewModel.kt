package com.pointcheck.features.auth.presentation

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Estado de la UI para la pantalla de inicio de sesión.
 *
 * @property email Correo electrónico ingresado por el usuario.
 * @property password Contraseña ingresada.
 * @property isValid Indica si el formulario cumple con las validaciones básicas (formato de email y longitud).
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isValid: Boolean = false
)

/**
 * ViewModel encargado de la lógica de la pantalla de Login.
 * Maneja el estado de los campos de entrada y realiza la validación reactiva en el cliente.
 *
 * @param application Contexto de la aplicación.
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    /**
     * Actualiza un campo específico del estado de la UI y recalcula la validez del formulario.
     *
     * @param field Nombre del campo a actualizar ("email" o "password").
     * @param value Nuevo valor ingresado.
     */
    fun onValueChange(field: String, value: String) {
        val s = _uiState.value
        _uiState.value = when(field) {
            "email" -> s.copy(email = value, isValid = isValid(value, s.password))
            "password" -> s.copy(password = value, isValid = isValid(s.email, value))
            else -> s
        }
    }

    /**
     * Realiza una validación básica del formato de email y longitud mínima de contraseña.
     *
     * @param email Email a validar.
     * @param pass Contraseña a validar.
     * @return true si ambos campos son válidos.
     */
    private fun isValid(email: String, pass: String) =
        Patterns.EMAIL_ADDRESS.matcher(email).matches() && pass.length >= 6
}
