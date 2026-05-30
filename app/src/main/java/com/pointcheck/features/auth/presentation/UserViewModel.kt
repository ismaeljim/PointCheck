package com.pointcheck.features.auth.presentation

import android.app.Application
import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.features.auth.data.dto.ServiceOfferingDto
import com.pointcheck.features.auth.data.dto.RegisterRequestDto
import com.pointcheck.features.auth.data.repository.AuthRepository
import com.pointcheck.features.profile.data.repository.ProfessionalProfileRepository
import com.pointcheck.core.util.RutUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val rut: String = "",
    val phone: String = "",
    val password: String = "",
    val confirm: String = "",
    val role: String = "CLIENT",
    val city: String = "",
    val address: String = "",
    val categoryId: String? = null,
    val selectedServices: List<ServiceOfferingDto> = emptyList(),
    val avatarUri: String? = null,
    val isValid: Boolean = false,
    val error: String? = null,
    val isLoading: Boolean = false
)

class UserViewModel(app: Application) : AndroidViewModel(app) {
    private val authRepository = AuthRepository()
    private val profileRepository = ProfessionalProfileRepository(ApiClient.instance)
    private val prefs = UserPreferences(app)
    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state

    init {
        viewModelScope.launch {
            prefs.role.collect { role ->
                _state.update { it.copy(role = role ?: "CLIENT") }
            }
        }
    }

    fun onValueChange(field: String, value: String) {
        val s = _state.value
        val n = when (field) {
            "name" -> s.copy(name = value)
            "email" -> s.copy(email = value)
            "rut" -> s.copy(rut = value.replace(".", "").replace("-", ""))
            "phone" -> s.copy(phone = value)
            "password" -> s.copy(password = value)
            "confirm" -> s.copy(confirm = value)
            "role" -> s.copy(role = value)
            "city" -> s.copy(city = value)
            "address" -> s.copy(address = value)
            "categoryId" -> s.copy(categoryId = value)
            else -> s
        }
        _state.value = n.copy(isValid = validate(n), error = null)
    }

    fun onServicesSelected(services: List<ServiceOfferingDto>) {
        val n = _state.value.copy(selectedServices = services)
        _state.value = n.copy(isValid = validate(n))
    }

    private fun validate(s: RegisterUiState): Boolean {
        val baseValid = s.name.isNotBlank()
                && Patterns.EMAIL_ADDRESS.matcher(s.email).matches()
                && RutUtils.validateRut(s.rut)
                && s.phone.length >= 8
                && s.password.length >= 6 
                && s.password == s.confirm
        
        return if (s.role == "SPECIALIST" || s.role == "PROFESSIONAL") {
            baseValid && s.city.isNotBlank() && s.address.isNotBlank() && s.categoryId != null
        } else {
            baseValid
        }
    }

    fun setAvatar(uri: Uri) {
        val s = _state.value
        _state.value = s.copy(avatarUri = uri.toString(), isValid = validate(s))
    }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        if (!s.isValid) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val registerRequest = RegisterRequestDto(
                name = s.name,
                email = s.email,
                password = s.password,
                rut = s.rut,
                phone = s.phone,
                role = s.role,
                city = if (s.role != "CLIENT") s.city else null,
                address = if (s.role != "CLIENT") s.address else null,
                categoryId = s.categoryId,
                services = if (s.role != "CLIENT") s.selectedServices else null
            )
            
            authRepository.register(registerRequest)
                .onSuccess { userResponse ->
                    _state.update { it.copy(isLoading = false) }
                    prefs.saveSession(
                        userId = userResponse.id,
                        name = userResponse.name,
                        email = userResponse.email,
                        role = userResponse.role,
                        phone = userResponse.phone,
                        rut = userResponse.rut
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
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Error en el registro") }
                }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.login(email.trim(), password)
                .onSuccess { userResponse ->
                    // Guardar sesión antes de notificar éxito
                    prefs.saveSession(
                        userId = userResponse.id,
                        name = userResponse.name,
                        email = userResponse.email,
                        role = userResponse.role,
                        phone = userResponse.phone,
                        rut = userResponse.rut
                    )
                    
                    if (userResponse.role == "SPECIALIST" || userResponse.role == "PROFESSIONAL") {
                        profileRepository.getProfileByUserId(userResponse.id)
                            .onSuccess { profile ->
                                prefs.saveProfessionalProfileId(profile.id)
                            }
                    }
                    
                    _state.update { it.copy(isLoading = false) }
                    onResult(true)
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Error de credenciales") }
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
