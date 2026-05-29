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

data class ServiceUiState(
    val services: List<ServiceResponseDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class ServiceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ServiceRepository(ApiClient.instance)
    private val prefs = UserPreferences(application)

    private val _state = MutableStateFlow(ServiceUiState())
    val state: StateFlow<ServiceUiState> = _state

    init {
        loadServices()
    }

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

    fun clearError() = _state.update { it.copy(error = null) }
    fun clearSuccess() = _state.update { it.copy(successMessage = null) }
}
