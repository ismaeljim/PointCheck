package com.pointcheck.features.external.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.core.network.NetworkHandler
import com.pointcheck.features.external.data.dto.WeatherResponseDto
import com.pointcheck.core.network.ApiException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Representa el estado de la interfaz de usuario para integraciones con APIs externas (ej. Clima).
 *
 * @property weatherData Datos climáticos recuperados.
 * @property isLoading Indica si hay una consulta externa en curso.
 * @property error Mensaje de error en caso de fallo en la integración.
 * @property successMessage Mensaje de éxito tras una operación exitosa.
 */
data class ExternalApiUiState(
    val weatherData: WeatherResponseDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel que gestiona las llamadas a servicios externos que no pertenecen al núcleo del backend.
 * Actualmente se encarga de la integración con el servicio meteorológico para informar a clientes y profesionales.
 */
class ExternalApiViewModel : ViewModel() {
    private val api = ApiClient.instance
    
    private val _state = MutableStateFlow(ExternalApiUiState())
    val state: StateFlow<ExternalApiUiState> = _state

    /**
     * Consulta el clima actual para una ciudad específica.
     *
     * @param city Nombre de la ciudad para la consulta meteorológica.
     */
    fun fetchWeather(city: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = api.getWeather(city)
                val result = NetworkHandler.handleResponse(response, "No se pudo obtener el clima")
                
                result.onSuccess { data ->
                    _state.update { it.copy(weatherData = data, isLoading = false) }
                }.onFailure { e ->
                    if (e is CancellationException) throw e
                    if (e is ApiException && (e.code == 401 || e.code == 403)) {
                        _state.update { it.copy(isLoading = false) }
                        return@onFailure
                    }
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                if (e is ApiException && (e.code == 401 || e.code == 403)) {
                    _state.update { it.copy(isLoading = false) }
                    return@launch
                }
                val result = NetworkHandler.handleException(e)
                _state.update { it.copy(error = result.exceptionOrNull()?.message, isLoading = false) }
            }
        }
    }

    /** Limpia el mensaje de error del estado. */
    fun clearError() = _state.update { it.copy(error = null) }
    /** Limpia el mensaje de éxito del estado. */
    fun clearSuccess() = _state.update { it.copy(successMessage = null) }
}
