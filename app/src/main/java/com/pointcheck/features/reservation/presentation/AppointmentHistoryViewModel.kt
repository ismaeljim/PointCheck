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

data class AppointmentHistoryUiState(
    val appointments: List<ReservationResponseDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val type: String = "recent" // "recent", "upcoming", "history"
)

class AppointmentHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ReservationRepository(ApiClient.instance)
    private val prefs = UserPreferences(application)

    private val _state = MutableStateFlow(AppointmentHistoryUiState())
    val state: StateFlow<AppointmentHistoryUiState> = _state

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
}
