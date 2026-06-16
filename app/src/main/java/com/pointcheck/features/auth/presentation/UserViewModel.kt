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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log

/**
 * Estado de la UI para el proceso de registro y gestión de usuario.
 * Centraliza los datos del formulario, estados de carga y errores.
 *
 * @property name Nombre completo del usuario.
 * @property email Correo electrónico.
 * @property rut Rol Único Tributario (RUT) chileno.
 * @property phone Número de teléfono de contacto.
 * @property password Contraseña de acceso.
 * @property confirm Confirmación de contraseña.
 * @property role Rol asignado al usuario (ej. "CLIENT", "SPECIALIST").
 * @property city Ciudad (requerido para especialistas).
 * @property address Dirección (requerido para especialistas).
 * @property categoryId ID de la categoría de servicios (requerido para especialistas).
 * @property selectedServices Lista de servicios ofrecidos por el especialista.
 * @property avatarUri URI local de la imagen de perfil seleccionada.
 * @property isValid Indica si el formulario actual cumple con todas las reglas de validación.
 * @property error Mensaje de error a mostrar en la UI.
 * @property isLoading Indica si se está realizando una operación asíncrona (ej. registro, login).
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
    val isLoading: Boolean = false,
    val userAddress: String? = null,
    val userName: String = "",
    val userPhone: String = "",
    val userEmail: String = "",
    val userRole: String = ""
)

/**
 * ViewModel principal para la gestión de usuarios y autenticación en la App.
 * Maneja tanto el registro de nuevos usuarios (Clientes/Especialistas) como el inicio de sesión.
 * 
 * AUDITORÍA DE REFRACTORIZACIÓN (UUID):
 * - Todos los identificadores (userId, profileId) ahora se manejan como String (UUID).
 * - Se ha verificado que la persistencia en DataStore coincida con estos tipos.
 * 
 * NOTA DE SEGURIDAD:
 * - La lógica de 'save' asume que el avatarUri es una cadena persistente, 
 *   pero las URIs de MediaPicker pueden caducar si no se toman persistencias de permisos.
 */
class UserViewModel(app: Application) : AndroidViewModel(app) {
    private val authRepository = AuthRepository()
    private val profileRepository = ProfessionalProfileRepository(ApiClient.instance)
    private val prefs = UserPreferences(app)
    
    // Estado observable por la UI
    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state

    init {
        // Al inicializar, cargamos los datos guardados
        viewModelScope.launch {
            prefs.role.collect { role ->
                _state.update { it.copy(role = role ?: "CLIENT", userRole = role ?: "CLIENT") }
            }
        }
        viewModelScope.launch {
            prefs.address.collect { address ->
                _state.update { it.copy(userAddress = address) }
            }
        }
        viewModelScope.launch {
            prefs.name.collect { name ->
                _state.update { it.copy(userName = name ?: "") }
            }
        }
        viewModelScope.launch {
            prefs.phone.collect { phone ->
                _state.update { it.copy(userPhone = phone ?: "") }
            }
        }
        viewModelScope.launch {
            prefs.email.collect { email ->
                _state.update { it.copy(userEmail = email ?: "") }
            }
        }
    }

    /**
     * Actualiza el perfil completo del usuario.
     */
    fun updateProfile(name: String, phone: String, address: String) {
        viewModelScope.launch {
            val userId = prefs.userId.first() ?: return@launch
            _state.update { it.copy(isLoading = true, error = null) }
            
            val request = com.pointcheck.features.auth.data.dto.UserUpdateRequestDto(
                name = name,
                phone = phone,
                address = address
            )

            authRepository.updateProfile(userId, request)
                .onSuccess { user ->
                    prefs.saveSession(
                        token = user.token,
                        userId = user.id,
                        name = user.name,
                        email = user.email,
                        role = user.role,
                        phone = user.phone,
                        rut = user.rut,
                        address = user.address
                    )
                    _state.update { it.copy(
                        isLoading = false,
                        userName = user.name ?: "",
                        userPhone = user.phone ?: "",
                        userAddress = user.address ?: ""
                    ) }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    /**
     * Actualiza la dirección del usuario autenticado.
     */
    fun updateAddress(newAddress: String) {
        viewModelScope.launch {
            val userId = prefs.userId.first() ?: return@launch
            _state.update { it.copy(isLoading = true) }
            authRepository.updateUserAddress(userId, newAddress)
                .onSuccess { user ->
                    prefs.saveSession(
                        token = user.token,
                        userId = user.id,
                        name = user.name,
                        email = user.email,
                        role = user.role,
                        phone = user.phone,
                        rut = user.rut,
                        address = user.address
                    )
                    _state.update { it.copy(isLoading = false, userAddress = user.address ?: "") }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    /**
     * Actualiza un campo específico del estado del formulario y dispara la validación.
     *
     * @param field Nombre del campo a actualizar (ej. "name", "email", "rut").
     * @param value Nuevo valor para el campo.
     */
    fun onValueChange(field: String, value: String) {
        val s = _state.value
        val n = when (field) {
            "name" -> s.copy(name = value)
            "email" -> s.copy(email = value)
            // Eliminamos caracteres especiales del RUT para estandarizar en la DB
            "rut" -> s.copy(rut = value.replace(".", "").replace("-", ""))
            "phone" -> s.copy(phone = value)
            "password" -> s.copy(password = value)
            "confirm" -> s.copy(confirm = value)
            "role" -> s.copy(role = value)
            "city" -> s.copy(city = value)
            "address" -> s.copy(address = value)
            "categoryId" -> s.copy(categoryId = value) // value es un UUID String
            else -> s
        }
        _state.value = n.copy(isValid = validate(n), error = null)
    }

    /**
     * Gestión de servicios seleccionados para el rol de Especialista.
     *
     * @param services Lista de DTOs que representan los servicios seleccionados.
     */
    fun onServicesSelected(services: List<ServiceOfferingDto>) {
        val n = _state.value.copy(selectedServices = services)
        _state.value = n.copy(isValid = validate(n))
    }

    /**
     * Lógica de validación del formulario de registro.
     * Implementa reglas de negocio como longitud de password y validez de RUT.
     */
    private fun validate(s: RegisterUiState): Boolean {
        val baseValid = s.name.isNotBlank()
                && Patterns.EMAIL_ADDRESS.matcher(s.email).matches()
                && RutUtils.validateRut(s.rut)
                && s.phone.length >= 8
                && s.password.length >= 6 
                && s.password == s.confirm
        
        // Validación extra si es un perfil prestador de servicios
        return if (s.role == "SPECIALIST" || s.role == "PROFESSIONAL") {
            baseValid && s.city.isNotBlank() && s.address.isNotBlank() && s.categoryId != null
        } else {
            baseValid
        }
    }

    /**
     * Establece la URI del avatar seleccionado desde el picker.
     *
     * @param uri URI de la imagen seleccionada.
     */
    fun setAvatar(uri: Uri) {
        val s = _state.value
        _state.value = s.copy(avatarUri = uri.toString(), isValid = validate(s))
    }

    /**
     * Registra el usuario en el backend.
     * Tras el éxito, se guardan los datos en preferencias locales para mantener la sesión.
     *
     * @param onDone Callback que se invoca tras un registro exitoso.
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
                    // Persistencia local de los datos básicos del usuario
                    prefs.saveSession(
                        token = userResponse.token,
                        userId = userResponse.id, // UUID
                        name = userResponse.name,
                        email = userResponse.email,
                        role = userResponse.role,
                        phone = userResponse.phone,
                        rut = userResponse.rut,
                        address = userResponse.address
                    )
                    
                    // Si es especialista, intentamos vincular el ID de perfil profesional inmediatamente
                    if (userResponse.role == "SPECIALIST" || userResponse.role == "PROFESSIONAL") {
                        userResponse.id?.let { id ->
                            profileRepository.getProfileByUserId(id)
                                .onSuccess { profile ->
                                    prefs.saveProfessionalProfileId(profile.id)
                                }
                                .onFailure { e ->
                                    Log.e("UserViewModel", "Perfil no creado automáticamente: ${e.message}")
                                }
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
     * Proceso de autenticación.
     * Recupera el token/perfil del backend y establece la sesión local.
     *
     * @param email Correo electrónico del usuario.
     * @param password Contraseña.
     * @param onResult Callback que retorna true si el login fue exitoso, false en caso contrario.
     */
    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.login(email.trim(), password)
                .onSuccess { userResponse ->
                    saveUserSession(userResponse)
                    _state.update { it.copy(isLoading = false) }
                    onResult(true)
                }
                .onFailure { e ->
                    // Eliminamos el fallback a Mock Data para sincerar los errores de red/auth
                    Log.e("UserViewModel", "Login fallido: ${e.message}")
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Error de autenticación") }
                    onResult(false)
                }
        }
    }

    private suspend fun saveUserSession(userResponse: com.pointcheck.features.auth.data.dto.UserResponseDto) {
        // Guardamos la sesión encriptada/segura en DataStore
        prefs.saveSession(
            token = userResponse.token,
            userId = userResponse.id,
            name = userResponse.name,
            email = userResponse.email,
            role = userResponse.role,
            phone = userResponse.phone,
            rut = userResponse.rut,
            address = userResponse.address
        )
        
        // Recuperación proactiva del ID de especialista para facilitar navegación en el Dashboard
        if (userResponse.role == "SPECIALIST" || userResponse.role == "PROFESSIONAL") {
            userResponse.id?.let { id ->
                profileRepository.getProfileByUserId(id)
                    .onSuccess { profile ->
                        prefs.saveProfessionalProfileId(profile.id)
                    }
                    .onFailure { e ->
                        Log.e("UserViewModel", "Error recuperando ID de perfil: ${e.message}")
                    }
            }
        }
    }

    /**
     * Cierra la sesión y limpia el almacenamiento local.
     *
     * @param onDone Callback que se invoca tras limpiar las preferencias.
     */
    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            prefs.clear()
            onDone()
        }
    }

    /**
     * Cambia la contraseña del usuario.
     */
    fun changePassword(current: String, new: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val userId = prefs.userId.first() ?: return@launch
            _state.update { it.copy(isLoading = true, error = null) }
            
            authRepository.changePassword(userId, current, new)
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    onResult(true, null)
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                    onResult(false, e.message)
                }
        }
    }
}
