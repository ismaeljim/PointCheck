package com.pointcheck.features.services.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.features.services.data.dto.ServiceRequestDto
import com.pointcheck.features.services.data.dto.ServiceResponseDto
import com.pointcheck.features.services.data.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Representa el estado de la interfaz de usuario para la gestión del catálogo de servicios.
 *
 * @property services Lista de servicios configurados por el profesional.
 * @property isLoading Indica si hay una operación de carga o mutación en curso.
 * @property error Mensaje de error para mostrar en la interfaz.
 * @property successMessage Mensaje de éxito tras crear o eliminar un servicio.
 */
data class ServiceUiState(
    val services: List<ServiceResponseDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel que gestiona el catálogo de servicios ofrecidos por un profesional.
 * Permite listar, agregar y eliminar servicios vinculados al perfil del usuario autenticado.
 *
 * @param application Contexto de la aplicación.
 */
class ServiceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ServiceRepository(ApiClient.instance)
    private val prefs = UserPreferences(application)

    private val _state = MutableStateFlow(ServiceUiState())
    val state: StateFlow<ServiceUiState> = _state

    init {
        loadServices()
    }

    /**
     * Recupera la lista de servicios asociados al perfil profesional del usuario.
     * Si el ID de perfil no está en caché local, intenta recuperarlo del servidor usando el ID de usuario.
     */
    fun loadServices() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            // Intentamos obtener el ID de perfil. Si es nulo, es posible que el perfil se haya creado 
            // en esta sesión, por lo que intentamos cargarlo del backend si no está en prefs.
            var profileId = prefs.professionalProfileId.first()
            
            if (profileId == null) {
                val userId = prefs.userId.first()
                if (userId != null) {
                    try {
                        val response = ApiClient.instance.getProfessionalProfileByUserId(userId)
                        if (response.isSuccessful) {
                            response.body()?.let { 
                                profileId = it.id
                                prefs.saveProfessionalProfileId(it.id)
                            }
                        }
                    } catch (_: Exception) { /* Silencioso */ }
                }
            }

            if (profileId == null) {
                _state.update { it.copy(isLoading = false, error = "No se encontró perfil profesional") }
                return@launch
            }

            repository.getServices(profileId!!)
                .onSuccess { list -> _state.update { it.copy(services = list, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    /**
     * Registra un nuevo servicio en el catálogo del profesional.
     *
     * @param name Nombre del servicio.
     * @param description Descripción detallada.
     * @param price Precio base.
     * @param duration Duración estimada en minutos.
     */
    fun addService(name: String, description: String, price: Double, duration: Int) {
        viewModelScope.launch {
            val profileId = prefs.professionalProfileId.first() ?: run {
                _state.update { it.copy(error = "No se encontró perfil profesional") }
                return@launch
            }
            _state.update { it.copy(isLoading = true, error = null) }
            val request = ServiceRequestDto(profileId, name, description, price, duration)
            repository.createService(request)
                .onSuccess { 
                    _state.update { it.copy(successMessage = "Servicio creado exitosamente") }
                    loadServices() 
                }
                .onFailure { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    /**
     * Elimina permanentemente un servicio del catálogo.
     *
     * @param id Identificador único (UUID) del servicio a eliminar.
     */
    fun deleteService(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.deleteService(id)
                .onSuccess { 
                    _state.update { it.copy(successMessage = "Servicio eliminado exitosamente") }
                    loadServices() 
                }
                .onFailure { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    /** Limpia el mensaje de error del estado. */
    fun clearError() = _state.update { it.copy(error = null) }
    /** Limpia el mensaje de éxito del estado. */
    fun clearSuccess() = _state.update { it.copy(successMessage = null) }
}
