package com.pointcheck.features.attentions.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.features.attentions.data.dto.AttentionRequestDto
import com.pointcheck.features.attentions.data.dto.AttentionResponseDto
import com.pointcheck.features.attentions.data.repository.AttentionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AttentionUiState(
    val currentAttention: AttentionResponseDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFinished: Boolean = false
)

class AttentionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AttentionRepository(ApiClient.instance)

    private val _state = MutableStateFlow(AttentionUiState())
    val state: StateFlow<AttentionUiState> = _state

    fun loadAttentionForReservation(reservationId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getAttentionByReservation(reservationId)
                .onSuccess { attention ->
                    _state.update { it.copy(currentAttention = attention, isLoading = false) }
                }
                .onFailure { e ->
                    // Si no existe, simplemente dejamos currentAttention como null
                    _state.update { it.copy(isLoading = false) }
                }
        }
    }

    fun startAttention(reservationId: Long, clientId: Long, specialistId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val request = AttentionRequestDto(reservationId, clientId, specialistId)
            repository.startAttention(request)
                .onSuccess { attention ->
                    _state.update { it.copy(currentAttention = attention, isLoading = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = "Error al iniciar atención: ${e.message}", isLoading = false) }
                }
        }
    }

    fun finishAttention(observations: String) {
        val attentionId = _state.value.currentAttention?.id ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.finishAttention(attentionId, observations)
                .onSuccess { updated ->
                    _state.update { it.copy(currentAttention = updated, isLoading = false, isFinished = true) }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = "Error al finalizar atención: ${e.message}", isLoading = false) }
                }
        }
    }
    
    fun resetState() {
        _state.value = AttentionUiState()
    }
}
