package com.pointcheck.features.auth.presentation

import android.app.Application
import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.features.auth.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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

    fun save(onDone: () -> Unit) {
        val s = _state.value; if (!s.isValid) return
        viewModelScope.launch {
            val user = User(email = s.email, name = s.name, password = s.password)
            try {
                val response = api.registerUser(user)
                if (response.isSuccessful) {
                    prefs.saveUser(s.name, s.email)
                    s.avatarUri?.let { prefs.setAvatar(it) }
                    onDone()
                } else {
                    _state.value = _state.value.copy(error = "Error en el registro")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Error de red: ${e.message}")
            }
        }
    }

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val loginUser = User(email = email, name = "", password = password) 
                val response = api.login(loginUser)
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        prefs.saveUser(user.name, user.email)
                        onResult(true)
                    } else {
                        _state.value = _state.value.copy(error = "Respuesta inválida")
                        onResult(false)
                    }
                } else {
                    _state.value = _state.value.copy(error = "Credenciales incorrectas")
                    onResult(false)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Error de red")
                onResult(false)
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            prefs.clear()
            onDone()
        }
    }
}
