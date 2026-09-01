package com.pointcheck.features.profile.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.features.profile.data.dto.ProfessionalProfileRequestDto
import com.pointcheck.features.profile.data.dto.ProfessionalProfileResponseDto
import com.pointcheck.features.profile.data.repository.ProfessionalProfileRepository
import com.pointcheck.core.util.MockDataProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pointcheck.features.auth.data.dto.UserUpdateRequestDto

/**
 * Configuración de las horas de trabajo para un día específico.
 *
 * @property start Hora de inicio en formato "HH:mm".
 * @property end Hora de fin en formato "HH:mm".
 * @property isActive Indica si el profesional está disponible para trabajar este día.
 */
data class DayConfig(
    val start: String = "09:00",
    val end: String = "18:00",
    val isActive: Boolean = false
)

/**
 * Estado de la interfaz de usuario para la pantalla de Perfil Profesional.
 *
 * @property profile Datos del perfil del profesional, si existen.
 * @property isLoading Indica si hay una solicitud de red en curso.
 * @property error Mensaje de error a mostrar si una operación falla.
 * @property successMessage Mensaje a mostrar tras operaciones exitosas.
 * @property isEditing Indica si la interfaz está actualmente en modo edición.
 * @property categories Lista de categorías de negocio/especialidades disponibles.
 * @property workingHours Mapa de nombres de días (ej., "MONDAY") con su respectiva [DayConfig].
 */
data class ProfessionalProfileUiState(
    val profile: ProfessionalProfileResponseDto? = null,
    val rut: String = "",
    val phone: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val successMessage: String? = null,
    val isEditing: Boolean = false,
    val categories: List<com.pointcheck.features.onboarding.presentation.dto.CategoryDto> = emptyList(),
    val workingHours: Map<String, DayConfig> = mapOf(
        "MONDAY" to DayConfig(),
        "TUESDAY" to DayConfig(),
        "WEDNESDAY" to DayConfig(),
        "THURSDAY" to DayConfig(),
        "FRIDAY" to DayConfig(),
        "SATURDAY" to DayConfig(),
        "SUNDAY" to DayConfig()
    )
)

/**
 * ViewModel responsable de gestionar el perfil del profesional, incluyendo detalles del negocio y horarios de trabajo.
 *
 * Este ViewModel maneja la obtención del perfil, la actualización de los horarios de disponibilidad y la persistencia de
 * los cambios en el servidor. También gestiona el estado para la selección de categorías y el modo edición.
 *
 * @param application El contexto de la aplicación.
 */
class ProfessionalProfileViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = ProfessionalProfileRepository(ApiClient.instance)
    private val categoryApi = ApiClient.retrofitInstance.create(com.pointcheck.features.onboarding.presentation.CategoryApi::class.java)
    private val prefs = UserPreferences(application)
    private val gson = Gson()
    
    private val _state = MutableStateFlow(ProfessionalProfileUiState())
    /**
     * Estado observable para la interfaz de usuario del Perfil Profesional.
     */
    val state: StateFlow<ProfessionalProfileUiState> = _state

    private val _navigationEvent = MutableStateFlow<String?>(null)
    /**
     * Evento de navegación para ser consumido por la UI.
     */
    val navigationEvent: StateFlow<String?> = _navigationEvent

    fun clearNavigationEvent() { _navigationEvent.value = null }

    init {
        loadProfile()
        loadCategories()
    }

    /**
     * Obtiene las categorías disponibles de la API para poblar el selector de especialidades.
     */
    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val cats = categoryApi.getCategories()
                _state.update { it.copy(categories = cats) }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                if (e is com.pointcheck.core.network.ApiException && (e.code == 401 || e.code == 403)) return@launch
                _state.update { it.copy(error = "Error al cargar categorías") }
            }
        }
    }

    /**
     * Actualiza la configuración de horario local para un día específico.
     *
     * @param day El nombre del día (ej., "MONDAY").
     * @param config La nueva [DayConfig] para ese día.
     */
    fun updateDayConfig(day: String, config: DayConfig) {
        _state.update { currentState ->
            val newHours = currentState.workingHours.toMutableMap()
            newHours[day] = config
            currentState.copy(workingHours = newHours)
        }
    }

    /**
     * Obtiene el perfil profesional del servidor para el usuario actualmente autenticado.
     *
     * Deserializa los horarios de trabajo desde JSON y persiste el ID del perfil localmente.
     */
    fun loadProfile() {
        viewModelScope.launch {
            val userId = prefs.userId.first() ?: return@launch
            val savedRut = prefs.rut.first() ?: ""
            val savedPhone = prefs.phone.first() ?: ""
            _state.update { it.copy(isLoading = true, error = null, rut = savedRut, phone = savedPhone) }
            
            repository.getProfileByUserId(userId)
                .onSuccess { profile ->
                    val workingHours = try {
                        if (!profile.workingHoursJson.isNullOrBlank()) {
                            val type = object : TypeToken<Map<String, DayConfig>>() {}.type
                            gson.fromJson<Map<String, DayConfig>>(profile.workingHoursJson, type)
                        } else {
                            _state.value.workingHours
                        }
                    } catch (e: Exception) {
                        _state.value.workingHours
                    }

                    _state.update { it.copy(profile = profile, isLoading = false, workingHours = workingHours) }
                    // Persistencia local del ID del perfil para facilitar los flujos de Reservas/Servicios
                    prefs.saveProfessionalProfileId(profile.id)
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    if (e is com.pointcheck.core.network.ApiException && e.code in listOf(401, 403)) return@onFailure
                    _state.update { it.copy(profile = null, isLoading = false) }
                }
        }
    }

    /**
     * Guarda o actualiza el perfil profesional en el servidor.
     *
     * Valida que las horas de inicio sean anteriores a las de fin para todos los días activos.
     * Serializa solo los horarios de trabajo activos a JSON antes de enviar la solicitud.
     *
     * @param categoryId ID de la categoría seleccionada.
     * @param displayName Nombre que se mostrará públicamente.
     * @param businessName Nombre del negocio.
     * @param specialty Descripción de la especialidad específica.
     * @param description Descripción detallada de los servicios ofrecidos.
     * @param address Dirección física del negocio.
     * @param city Ciudad donde se ubica el negocio.
     * @param duration Duración predeterminada de la sesión en minutos.
     * @param rut RUT del profesional para validación.
     * @param phone Teléfono de contacto.
     * @param latitude Coordenada de latitud GPS opcional.
     * @param longitude Coordenada de longitud GPS opcional.
     * @param updateBaseAddress Si es verdadero, actualiza también la dirección base del usuario.
     */
    fun saveProfile(
        categoryId: String?,
        displayName: String,
        businessName: String,
        specialty: String,
        description: String,
        address: String,
        city: String,
        duration: Int,
        rut: String,
        phone: String,
        latitude: Double? = null,
        longitude: Double? = null,
        updateBaseAddress: Boolean = false
    ) {
        viewModelScope.launch {
            val userId = prefs.userId.first() ?: return@launch

            // Validaciones de RUT y Teléfono
            if (!com.pointcheck.core.util.RutUtils.validateRut(rut)) {
                _state.update { it.copy(error = "El RUT ingresado no es válido.") }
                return@launch
            }

            if (phone.length < 8) {
                _state.update { it.copy(error = "El teléfono debe tener al menos 8 dígitos.") }
                return@launch
            }

            // Validación de lógica de negocio: Horarios coherentes
            val invalidDays = _state.value.workingHours.filter { it.value.isActive }.filter { 
                val start = it.value.start.split(":").let { (h, m) -> h.toInt() * 60 + m.toInt() }
                val end = it.value.end.split(":").let { (h, m) -> h.toInt() * 60 + m.toInt() }
                start >= end
            }

            if (invalidDays.isNotEmpty()) {
                _state.update { it.copy(error = "La hora de inicio debe ser anterior a la de fin para los días activos.") }
                return@launch
            }

            _state.update { it.copy(isLoading = true, error = null) }

            // Serializa los horarios de trabajo solo para los días activos
            val activeHours = _state.value.workingHours.filter { it.value.isActive }
            val workingHoursJson = gson.toJson(activeHours)

            val request = ProfessionalProfileRequestDto(
                userId = userId,
                categoryId = categoryId,
                displayName = displayName,
                businessName = businessName,
                specialty = specialty,
                description = description,
                address = address,
                city = city,
                defaultSessionDurationMinutes = duration,
                latitude = latitude,
                longitude = longitude,
                workingHoursJson = workingHoursJson
            )

            val currentProfile = _state.value.profile
            val result = if (currentProfile == null) {
                repository.createProfile(request)
            } else {
                repository.updateProfile(currentProfile.id, request)
            }

            result.onSuccess { updated ->
                _state.update { it.copy(profile = updated, isLoading = false, successMessage = "Perfil guardado con éxito", isEditing = false) }
                prefs.saveProfessionalProfileId(updated.id)
                
                // Persistir Nombre y Teléfono en las preferencias locales y backend para consistencia
                viewModelScope.launch {
                    val authApi = ApiClient.retrofitInstance.create(com.pointcheck.core.network.ApiService::class.java)
                    try {
                        val updateResponse = authApi.updateUserProfile(userId, UserUpdateRequestDto(
                            name = displayName,
                            phone = phone,
                            address = if (updateBaseAddress) address else null
                        ))
                        
                        if (updateResponse.isSuccessful) {
                            val user = updateResponse.body()
                            if (user != null) {
                                prefs.saveSession(
                                    userId = user.id ?: "",
                                    name = user.name ?: "",
                                    email = user.email ?: "",
                                    role = user.role ?: "CLIENT",
                                    phone = user.phone ?: "",
                                    rut = user.rut ?: "",
                                    address = user.address ?: ""
                                )
                            }
                        }
                    } catch (e: Exception) {
                        if (e is kotlinx.coroutines.CancellationException) throw e
                        if (e is com.pointcheck.core.network.ApiException && (e.code == 401 || e.code == 403)) return@launch
                        // Error no crítico para el flujo principal
                    }
                }

                // Si es un perfil nuevo, redirigir a configuración de servicios para evitar el estado "Incompleto"
                if (currentProfile == null) {
                    _navigationEvent.value = "service_management"
                }
            }.onFailure { e ->
                if (e is CancellationException) throw e
                if (e is com.pointcheck.core.network.ApiException && e.code in listOf(401, 403)) return@onFailure
                _state.update { it.copy(isLoading = false, error = "Error al guardar perfil: ${e.message}") }
            }
        }
    }

    /**
     * Alterna la interfaz de usuario entre los modos de visualización y edición.
     */
    fun toggleEdit() {
        _state.update { it.copy(isEditing = !it.isEditing) }
    }

    /**
     * Limpia el mensaje de error actual del estado.
     */
    fun clearError() = _state.update { it.copy(error = null) }

    /**
     * Limpia el mensaje de éxito actual del estado.
     */
    fun clearSuccess() = _state.update { it.copy(successMessage = null) }
}
