package com.pointcheck.features.services.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.features.services.data.dto.ServiceRequestDto
import com.pointcheck.features.services.data.dto.ServiceResponseDto
import com.pointcheck.features.services.data.repository.ServiceRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Jerarquía de estados para la gestión de servicios.
 */
sealed class ServiceUiState {
    object Loading : ServiceUiState()
    data class Success(
        val services: List<ServiceResponseDto> = emptyList(),
        val successMessage: String? = null
    ) : ServiceUiState()
    data class Error(val message: String) : ServiceUiState()
}

/**
 * ViewModel que gestiona el catálogo de servicios ofrecidos por un profesional.
 */
class ServiceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ServiceRepository(ApiClient.instance)
    private val prefs = UserPreferences(application)

    private val _state = MutableStateFlow<ServiceUiState>(ServiceUiState.Loading)
    val state: StateFlow<ServiceUiState> = _state

    init {
        loadServices()
    }

    fun loadServices() {
        viewModelScope.launch {
            _state.value = ServiceUiState.Loading
            
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
                    } catch (_: Exception) { }
                }
            }

            if (profileId == null) {
                _state.value = ServiceUiState.Error("No se encontró perfil profesional")
                return@launch
            }

            repository.getServices(profileId!!)
                .onSuccess { list -> _state.value = ServiceUiState.Success(services = list) }
                .onFailure { e -> 
                    if (e is CancellationException) throw e
                    if (e is com.pointcheck.core.network.ApiException && e.code in listOf(401, 403)) return@onFailure
                    _state.value = ServiceUiState.Error(e.message ?: "Error al cargar servicios") 
                }
        }
    }

    fun addService(name: String, description: String, price: Double, duration: Int) {
        viewModelScope.launch {
            val profileId = prefs.professionalProfileId.first() ?: run {
                _state.value = ServiceUiState.Error("No se encontró perfil profesional")
                return@launch
            }
            _state.value = ServiceUiState.Loading
            val request = ServiceRequestDto(profileId, name, description, price, duration)
            repository.createService(request)
                .onSuccess { 
                    loadServices() 
                }
                .onFailure { e -> 
                    if (e is CancellationException) throw e
                    if (e is com.pointcheck.core.network.ApiException && e.code in listOf(401, 403)) return@onFailure
                    _state.value = ServiceUiState.Error(e.message ?: "Error al añadir servicio") 
                }
        }
    }

    fun deleteService(id: String) {
        viewModelScope.launch {
            _state.value = ServiceUiState.Loading
            repository.deleteService(id)
                .onSuccess { 
                    loadServices() 
                }
                .onFailure { e -> 
                    if (e is CancellationException) throw e
                    if (e is com.pointcheck.core.network.ApiException && e.code in listOf(401, 403)) return@onFailure
                    _state.value = ServiceUiState.Error(e.message ?: "Error al eliminar servicio") 
                }
        }
    }

    fun clearError() {
        if (_state.value is ServiceUiState.Error) {
            loadServices()
        }
    }
}
