package com.pointcheck.features.auth.presentation

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import androidx.lifecycle.viewModelScope
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.features.auth.data.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Jerarquía de estados para la pantalla de Login.
 */
sealed class LoginUiState {
    data class Input(
        val email: String = "",
        val password: String = "",
        val isValid: Boolean = false,
        val error: String? = null
    ) : LoginUiState()
    
    object Loading : LoginUiState()
    object Success : LoginUiState()
}

/**
 * ViewModel que gestiona la autenticación.
 * Implementa validación reactiva y comunicación con el backend.
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = AuthRepository()
    private val userPrefs = UserPreferences(application)

    private val _state = MutableStateFlow<LoginUiState>(LoginUiState.Input())
    val state: StateFlow<LoginUiState> = _state

    /**
     * Actualiza los campos de entrada y valida el formato.
     */
    fun onValueChange(field: String, value: String) {
        val current = _state.value
        if (current is LoginUiState.Input) {
            _state.value = when (field) {
                "email" -> current.copy(email = value, isValid = validate(value, current.password))
                "password" -> current.copy(password = value, isValid = validate(current.email, value))
                else -> current
            }
        }
    }

    private fun validate(email: String, pass: String) = 
        Patterns.EMAIL_ADDRESS.matcher(email).matches() && pass.length >= 4

    /**
     * Ejecuta la petición de login al backend real.
     */
    fun login() {
        val current = _state.value as? LoginUiState.Input ?: return
        
        viewModelScope.launch {
            _state.value = LoginUiState.Loading
            
            repository.login(current.email, current.password)
                .onSuccess { user ->
                    // Guardamos la sesión completa para evitar inconsistencias en el perfil
                    userPrefs.saveSession(
                        token = user.token,
                        userId = user.id ?: "",
                        name = user.name ?: "Usuario",
                        email = user.email ?: "",
                        role = user.role ?: "CLIENT",
                        phone = user.phone ?: "",
                        rut = user.rut ?: "",
                        address = user.address
                    )
                    _state.value = LoginUiState.Success
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    _state.value = LoginUiState.Input(
                        email = current.email,
                        password = current.password,
                        isValid = true,
                        error = e.localizedMessage ?: "Error de conexión con el servidor"
                    )
                }
        }
    }

    fun clearError() {
        val current = _state.value as? LoginUiState.Input ?: return
        _state.value = current.copy(error = null)
    }
}
