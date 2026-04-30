package com.pointcheck.features.auth.presentation

import android.app.Application
import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.features.auth.data.dto.RegisterRequestDto
import com.pointcheck.features.auth.data.repository.AuthRepository
import com.pointcheck.features.profile.data.repository.ProfessionalProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirm: String = "",
    val role: String = "CLIENT",
    val avatarUri: String? = null,
    val isValid: Boolean = false,
    val error: String? = null
)

class UserViewModel(app: Application) : AndroidViewModel(app) {
    private val authRepository = AuthRepository()
    private val profileRepository = ProfessionalProfileRepository(ApiClient.instance)
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
            "role" -> s.copy(role = value)
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
        val s = _state.value
        if (!s.isValid) return

        viewModelScope.launch {
            val registerRequest = RegisterRequestDto(
                name = s.name,
                email = s.email,
                password = s.password,
                phone = null,
                role = s.role
            )
            
            authRepository.register(registerRequest)
                .onSuccess { userResponse ->
                    prefs.saveSession(
                        userId = userResponse.id,
                        name = userResponse.name,
                        email = userResponse.email,
                        role = userResponse.role,
                        phone = userResponse.phone
                    )
                    
                    if (userResponse.role == "SPECIALIST" || userResponse.role == "PROFESSIONAL") {
                        profileRepository.getProfileByUserId(userResponse.id)
                            .onSuccess { profile ->
                                prefs.saveProfessionalProfileId(profile.id)
                            }
                    }

                    s.avatarUri?.let { prefs.setAvatar(it) }
                    onDone()
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(error = e.message ?: "Error en el registro")
                }
        }
    }

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            authRepository.login(email, password)
                .onSuccess { userResponse ->
                    prefs.saveSession(
                        userId = userResponse.id,
                        name = userResponse.name,
                        email = userResponse.email,
                        role = userResponse.role,
                        phone = userResponse.phone
                    )
                    
                    if (userResponse.role == "SPECIALIST" || userResponse.role == "PROFESSIONAL") {
                        profileRepository.getProfileByUserId(userResponse.id)
                            .onSuccess { profile ->
                                prefs.saveProfessionalProfileId(profile.id)
                            }
                    }
                    
                    onResult(true)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(error = e.message ?: "Error de credenciales")
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
