package com.pointcheck.features.profile.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.features.profile.data.dto.ProfessionalProfileRequestDto
import com.pointcheck.features.profile.data.dto.ProfessionalProfileResponseDto
import com.pointcheck.features.profile.data.repository.ProfessionalProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class DayConfig(
    val start: String = "09:00",
    val end: String = "18:00",
    val isActive: Boolean = false
)

data class ProfessionalProfileUiState(
    val profile: ProfessionalProfileResponseDto? = null,
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
 * ViewModel responsable de la gestión del Perfil Profesional del Especialista.
 * Administra el estado de la UI para la edición de datos comerciales, categorías
 * y configuración de horarios de disponibilidad.
 */
class ProfessionalProfileViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = ProfessionalProfileRepository(ApiClient.instance)
    private val categoryApi = ApiClient.retrofitInstance.create(com.pointcheck.features.onboarding.presentation.CategoryApi::class.java)
    private val prefs = UserPreferences(application)
    private val gson = Gson()
    
    private val _state = MutableStateFlow(ProfessionalProfileUiState())
    val state: StateFlow<ProfessionalProfileUiState> = _state

    init {
        loadProfile()
        loadCategories()
    }

    /**
     * Carga las categorías disponibles desde el API para poblar el selector de especialidades.
     */
    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val cats = categoryApi.getCategories()
                _state.update { it.copy(categories = cats) }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error al cargar categorías") }
            }
        }
    }

    /**
     * Actualiza localmente la configuración de un día específico antes de guardar.
     */
    fun updateDayConfig(day: String, config: DayConfig) {
        _state.update { currentState ->
            val newHours = currentState.workingHours.toMutableMap()
            newHours[day] = config
            currentState.copy(workingHours = newHours)
        }
    }

    /**
     * Carga el perfil desde el backend y deserializa los horarios de trabajo (JSON).
     * AUDITORÍA: Si el perfil no existe, se mantiene el estado inicial permitiendo la creación.
     */
    fun loadProfile() {
        viewModelScope.launch {
            val userId = prefs.userId.first() ?: return@launch
            _state.update { it.copy(isLoading = true, error = null) }
            
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
                    // Persistencia local del ID de perfil para facilitar flujos de Reservas/Servicios
                    prefs.saveProfessionalProfileId(profile.id)
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false) } 
                }
        }
    }

    /**
     * Persiste los cambios del perfil en el servidor.
     * AUDITORÍA: 
     * - Valida consistencia horaria (inicio < fin).
     * - Diferencia automáticamente entre creación (POST) y actualización (PUT).
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
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        viewModelScope.launch {
            val userId = prefs.userId.first() ?: return@launch

            // Validación de lógica de negocio: Horarios consistentes
            val invalidDays = _state.value.workingHours.filter { it.value.isActive }.filter { 
                val start = it.value.start.split(":").let { (h, m) -> h.toInt() * 60 + m.toInt() }
                val end = it.value.end.split(":").let { (h, m) -> h.toInt() * 60 + m.toInt() }
                start >= end
            }

            if (invalidDays.isNotEmpty()) {
                _state.update { it.copy(error = "La hora de inicio debe ser menor a la de fin en los días activos.") }
                return@launch
            }

            _state.update { it.copy(isLoading = true, error = null) }

            // Serialización de horarios solo para días marcados como activos
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
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = "Error al guardar perfil: ${e.message}") }
            }
        }
    }

    fun toggleEdit() {
        _state.update { it.copy(isEditing = !it.isEditing) }
    }

    fun clearError() = _state.update { it.copy(error = null) }
    fun clearSuccess() = _state.update { it.copy(successMessage = null) }
}
