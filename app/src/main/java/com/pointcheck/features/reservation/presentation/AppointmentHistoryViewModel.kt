package com.pointcheck.features.reservation.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.features.reservation.data.dto.ReservationResponseDto
import com.pointcheck.features.reservation.data.repository.ReservationRepository
import com.pointcheck.core.util.MockDataProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Jerarquía de estados para la pantalla de Historial de Citas.
 * Garantiza que la UI solo maneje un estado atómico a la vez.
 */
sealed class AppointmentHistoryUiState {
    object Loading : AppointmentHistoryUiState()
    
    data class Success(
        val appointments: List<ReservationResponseDto>,
        val type: String,
        val successMessage: String? = null
    ) : AppointmentHistoryUiState()
    
    data class Error(val message: String) : AppointmentHistoryUiState()
}

/**
 * ViewModel responsable de gestionar y obtener el historial de reservas de un cliente.
 * 
 * Implementa estados sellados para una navegación fluida y sin estados inconsistentes.
 * Utiliza un sistema de fallback (Mock) para asegurar que la demo siempre sea visualmente atractiva.
 *
 * @param application El contexto de la aplicación.
 */
class AppointmentHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ReservationRepository(ApiClient.instance)
    private val prefs = UserPreferences(application)

    private val _state = MutableStateFlow<AppointmentHistoryUiState>(AppointmentHistoryUiState.Loading)
    
    /** Estado observable de la interfaz. */
    val state: StateFlow<AppointmentHistoryUiState> = _state

    /**
     * Carga la lista de citas filtradas por tipo.
     * 
     * @param type Categoría de citas: "upcoming", "recent" o "history".
     */
    fun loadAppointments(type: String) {
        viewModelScope.launch {
            _state.value = AppointmentHistoryUiState.Loading
            val userId = prefs.userId.first()
            
            if (userId != null) {
                val result = when (type) {
                    "upcoming" -> repository.getUpcomingReservationsByClient(userId)
                    else -> repository.getReservationHistoryByClient(userId)
                }
                
                result.onSuccess { list ->
                    _state.value = AppointmentHistoryUiState.Success(
                        appointments = list,
                        type = type
                    )
                }.onFailure { e ->
                    if (e is CancellationException) throw e
                    if (e is com.pointcheck.core.network.ApiException && e.code in listOf(401, 403)) return@onFailure
                    _state.value = AppointmentHistoryUiState.Error(
                        e.localizedMessage ?: "No se pudo conectar con el servidor"
                    )
                }
            } else {
                _state.value = AppointmentHistoryUiState.Error("Sesión no válida")
            }
        }
    }
}

