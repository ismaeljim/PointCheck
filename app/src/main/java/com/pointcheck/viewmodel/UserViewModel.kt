package com.pointcheck.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.data.prefs.UserPreferences
import com.pointcheck.model.User
import com.pointcheck.repository.RoomRepository
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
    private val repo = RoomRepository(app)
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
            repo.registerUser(User(email = s.email, name = s.name, password = s.password))
            // Guardar datos del usuario en preferencias
            prefs.saveUser(s.name, s.email)
            // Guardar avatar si existe
            s.avatarUri?.let { prefs.setAvatar(it) }
            onDone()
        }
    }

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val u = repo.findUserByEmail(email)
            val ok = u != null && u.password == password
            if (ok && u != null) {
                // Guardar datos del usuario en preferencias
                prefs.saveName(u.name)
                prefs.saveEmail(u.email)
                u.email // El avatar se guarda si existe en el estado
            } else {
                _state.value = _state.value.copy(error = "Credenciales incorrectas")
            }
            onResult(ok)
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            prefs.clear()
            onDone()
        }
    }
}
