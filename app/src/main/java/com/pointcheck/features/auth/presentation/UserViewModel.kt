package com.pointcheck.features.auth.presentation

import android.app.Application
import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.features.auth.data.dto.LoginRequestDto
import com.pointcheck.features.auth.data.dto.RegisterRequestDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirm: String = "",
    val avatarUri: String? = null,
    val isValid: Boolean = false,
    val error: String? = null
)

class UserViewModel(app: Application) : AndroidViewModel(app) {
    private val api = ApiClient.instance
    private val prefs = UserPreferences(app)
    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state

    fun onValueChange(field: String, value: String) {
        val s = _state.value
        val n = when (field) {
            "name" -> s.copy(name = value)
            "email" -> s.copy(email = value)
            "password" -> s.copy(password = value)
            "confirm" -> s.copy(confirm = value)
            else -> s
        }
        _state.value = n.copy(isValid = validate(n), error = null)
    }

    private fun validate(s: RegisterUiState) = s.name.isNotBlank()
            && Patterns.EMAIL_ADDRESS.matcher(s.email).matches()
            && s.password.length >= 6 && s.password == s.confirm

    fun setAvatar(uri: Uri) {
        val s = _state.value
        _state.value = s.copy(avatarUri = uri.toString(), isValid = validate(s))
    }

    /**
     * Proceso de registro de usuario usando el backend Spring Boot.
     */
    fun save(onDone: () -> Unit) {
        val s = _state.value
        if (!s.isValid) return

        viewModelScope.launch {
            val registerRequest = RegisterRequestDto(
                name = s.name,
                email = s.email,
                password = s.password,
                phone = null,
                role = "CLIENT"
            )
            try {
                val response = api.registerUser(registerRequest)
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    if (userResponse != null) {
                        prefs.saveSession(
                            userId = userResponse.id,
                            name = userResponse.name,
                            email = userResponse.email,
                            role = userResponse.role,
                            phone = userResponse.phone
                        )
                        s.avatarUri?.let { prefs.setAvatar(it) }
                        onDone()
                    } else {
                        _state.value = _state.value.copy(error = "Respuesta inválida del servidor")
                    }
                } else {
                    handleApiError(response)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Error de red: Verifique su conexión")
            }
        }
    }

    /**
     * Proceso de login de usuario usando el backend Spring Boot.
     */
    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val loginRequest = LoginRequestDto(email = email, password = password)
                val response = api.login(loginRequest)
                
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    if (userResponse != null) {
                        prefs.saveSession(
                            userId = userResponse.id,
                            name = userResponse.name,
                            email = userResponse.email,
                            role = userResponse.role,
                            phone = userResponse.phone
                        )
                        onResult(true)
                    } else {
                        _state.value = _state.value.copy(error = "Respuesta inválida del servidor")
                        onResult(false)
                    }
                } else {
                    handleApiError(response)
                    onResult(false)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Error de red: Verifique su conexión")
                onResult(false)
            }
        }
    }

    /**
     * Mapea códigos de error HTTP a mensajes legibles.
     */
    private fun handleApiError(response: Response<*>) {
        val message = when (response.code()) {
            400 -> "Solicitud inválida"
            401, 403 -> "Credenciales incorrectas"
            409 -> "El correo ya está registrado"
            500 -> "Error interno del servidor"
            else -> "Error inesperado (${response.code()})"
        }
        _state.value = _state.value.copy(error = message)
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            prefs.clear()
            onDone()
        }
    }
}
