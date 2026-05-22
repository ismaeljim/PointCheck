package com.pointcheck.features.external.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.core.network.NetworkHandler
import com.pointcheck.features.external.data.dto.WeatherResponseDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExternalApiUiState(
    val weatherData: WeatherResponseDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class ExternalApiViewModel : ViewModel() {
    private val api = ApiClient.instance
    
    private val _state = MutableStateFlow(ExternalApiUiState())
    val state: StateFlow<ExternalApiUiState> = _state

    fun fetchWeather(city: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = api.getWeather(city)
                val result = NetworkHandler.handleResponse(response, "No se pudo obtener el clima")
                
                result.onSuccess { data ->
                    _state.update { it.copy(weatherData = data, isLoading = false) }
                }.onFailure { e ->
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
            } catch (e: Exception) {
                val result = NetworkHandler.handleException(e)
                _state.update { it.copy(error = result.exceptionOrNull()?.message, isLoading = false) }
            }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
    fun clearSuccess() = _state.update { it.copy(successMessage = null) }
}
