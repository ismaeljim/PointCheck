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

/**
 * Estado de la UI para el proceso de registro y gestión de usuario.
 * Centraliza los datos del formulario, estados de carga y errores.
 */
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

/**
 * ViewModel principal para la gestión de usuarios y autenticación en la App.
 * Maneja tanto el registro de nuevos usuarios (Clientes/Especialistas) como el inicio de sesión.
 * 
 * AUDITORÍA:
 * - Implementa persistencia local de sesión mediante UserPreferences.
 * - Soporta el flujo de registro en múltiples pasos para Especialistas.
 * - BUG POTENCIAL: La lógica de 'save' asume que el avatarUri es una cadena persistente, 
 *   pero las URIs de MediaPicker pueden caducar si no se toman persistencias de permisos.
 */
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

    /**
     * Valida el estado del formulario de registro.
     * 
     * AUDITORÍA:
     * - Se valida formato de Email, RUT y longitud mínima de campos.
     * - Para Especialistas, se obliga a tener Ciudad, Dirección y Categoría.
     */
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

    /**
     * Ejecuta el registro del usuario en el Backend.
     * Si tiene éxito, guarda la sesión localmente y recupera el perfil profesional si aplica.
     */
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
                            .onFailure { e ->
                                // Logueamos el error pero no bloqueamos el inicio de sesión
                                android.util.Log.e("UserViewModel", "Error cargando perfil: ${e.message}")
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

    /**
     * Realiza el login y establece la sesión local.
     * AUDITORÍA: Al iniciar sesión, se intenta recuperar el ProfessionalProfileId 
     * de forma proactiva para especialistas.
     */
    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.login(email.trim(), password)
                .onSuccess { userResponse ->
                    // Consolidado: Usamos solo saveSession para evitar inconsistencias
                    prefs.saveSession(
                        userId = userResponse.id,
                        name = userResponse.name,
                        email = userResponse.email,
                        role = userResponse.role,
                        phone = userResponse.phone,
                        rut = userResponse.rut
                    )
                    
                    // Si es especialista, guardamos proactivamente su ID de perfil
                    if (userResponse.role == "SPECIALIST" || userResponse.role == "PROFESSIONAL") {
                        profileRepository.getProfileByUserId(userResponse.id)
                            .onSuccess { profile ->
                                prefs.saveProfessionalProfileId(profile.id)
                            }
                            .onFailure { e ->
                                android.util.Log.e("UserViewModel", "Perfil no encontrado aún: ${e.message}")
                            }
                    }
                    
                    _state.update { it.copy(isLoading = false) }
                    onResult(true)
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Credenciales inválidas") }
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
