package com.pointcheck.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.data.network.NetworkRepository
import com.pointcheck.data.prefs.UserPreferences
import com.pointcheck.model.User
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
    private val networkRepo = NetworkRepository()
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
                val response = networkRepo.registerUser(user)
                if (response.isSuccessful) {
                    prefs.saveUser(s.name, s.email)
                    s.avatarUri?.let { prefs.setAvatar(it) }
                    onDone()
                } else {
                    _state.value = _state.value.copy(error = "Error en el registro")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Error de red")
            }
        }
    }

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // Para el login, creamos un objeto User, pero el servidor solo usará email y password
                val loginUser = User(email = email, name = "", password = password) 
                val response = networkRepo.login(loginUser)
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        prefs.saveUser(user.name, user.email)
                        onResult(true)
                    } else {
                        _state.value = _state.value.copy(error = "Respuesta inválida del servidor")
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
