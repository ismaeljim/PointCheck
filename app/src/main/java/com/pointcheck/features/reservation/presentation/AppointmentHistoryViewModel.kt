package com.pointcheck.features.reservation.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.features.reservation.data.dto.ReservationResponseDto
import com.pointcheck.features.reservation.data.repository.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de la interfaz de usuario para la pantalla de Historial de Citas.
 *
 * @property appointments Lista de reservas obtenidas del servidor.
 * @property isLoading Indica si hay una solicitud de red en curso.
 * @property error Mensaje de error a mostrar si una operación falla.
 * @property successMessage Mensaje a mostrar tras operaciones exitosas.
 * @property type El tipo de filtro para las citas (ej., "recent", "upcoming", "history").
 */
data class AppointmentHistoryUiState(
    val appointments: List<ReservationResponseDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val type: String = "recent" // "recent", "upcoming", "history"
)

/**
 * ViewModel responsable de gestionar y obtener el historial de reservas de un cliente.
 *
 * Proporciona funcionalidad para filtrar citas por estado (próximas o historial pasado)
 * basado en el usuario actualmente autenticado.
 *
 * @param application El contexto de la aplicación.
 */
class AppointmentHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ReservationRepository(ApiClient.instance)
    private val prefs = UserPreferences(application)

    private val _state = MutableStateFlow(AppointmentHistoryUiState())
    /**
     * Estado observable para la interfaz de usuario del Historial de Citas.
     */
    val state: StateFlow<AppointmentHistoryUiState> = _state

    /**
     * Carga la lista de citas para el usuario actual basada en el tipo especificado.
     *
     * @param type La categoría de citas a cargar: "upcoming", "recent" o "history".
     */
    fun loadAppointments(type: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, type = type) }
            val userId = prefs.userId.first()
            
            if (userId != null) {
                val result = when (type) {
                    "upcoming" -> repository.getUpcomingReservationsByClient(userId)
                    "recent", "history" -> repository.getReservationHistoryByClient(userId)
                    else -> repository.getReservationHistoryByClient(userId)
                }
                
                result.onSuccess { list ->
                    _state.update { it.copy(appointments = list, isLoading = false) }
                }.onFailure { e ->
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
            } else {
                _state.update { it.copy(isLoading = false, error = "Usuario no identificado") }
            }
        }
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

