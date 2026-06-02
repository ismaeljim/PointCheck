package com.pointcheck.features.auth.presentation

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Estado de la UI para la pantalla de inicio de sesión.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isValid: Boolean = false
)

/**
 * ViewModel para la lógica de la pantalla de Login.
 * Maneja el estado de los campos de entrada y la validación básica en el cliente.
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    /**
     * Actualiza el estado de la UI cuando el usuario escribe en los campos.
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
     * Validación simple de formato de email y longitud de contraseña.
     * 
     * AUDITORÍA:
     * - Se debe considerar agregar feedback visual específico para cada tipo de error (ej: "Email inválido").
     */
    private fun isValid(email: String, pass: String) =
        Patterns.EMAIL_ADDRESS.matcher(email).matches() && pass.length >= 6
}
