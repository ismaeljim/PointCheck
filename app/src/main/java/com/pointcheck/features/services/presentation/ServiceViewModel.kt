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
    val error: String? = null
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
            val profileId = prefs.professionalProfileId.first() ?: return@launch
            _state.update { it.copy(isLoading = true) }
            repository.getServices(profileId)
                .onSuccess { list -> _state.update { it.copy(services = list, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    fun addService(name: String, description: String, price: Double, duration: Int) {
        viewModelScope.launch {
            val profileId = prefs.professionalProfileId.first() ?: return@launch
            _state.update { it.copy(isLoading = true) }
            val request = ServiceRequestDto(profileId, name, description, price, duration)
            repository.createService(request)
                .onSuccess { loadServices() }
                .onFailure { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    fun deleteService(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.deleteService(id)
                .onSuccess { loadServices() }
                .onFailure { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
        }
    }
}
