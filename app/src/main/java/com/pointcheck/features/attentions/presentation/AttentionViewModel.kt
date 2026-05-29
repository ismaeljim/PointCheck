package com.pointcheck.features.attentions.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.features.attentions.data.dto.AttentionResponseDto
import com.pointcheck.features.attentions.data.repository.AttentionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AttentionUiState(
    val currentAttention: AttentionResponseDto? = null,
    val observations: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class AttentionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AttentionRepository(ApiClient.instance)

    private val _state = MutableStateFlow(AttentionUiState())
    val state: StateFlow<AttentionUiState> = _state

    fun setObservations(value: String) {
        _state.update { it.copy(observations = value) }
    }

    fun startAttention(reservationId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.startAttention(reservationId, _state.value.observations)
                .onSuccess { attention ->
                    _state.update { it.copy(
                        currentAttention = attention, 
                        isLoading = false,
                        successMessage = "Atención iniciada"
                    ) }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = "Error al iniciar: ${e.message}", isLoading = false) }
                }
        }
    }

    fun finishAttention() {
        val attentionId = _state.value.currentAttention?.id ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.finishAttention(attentionId, _state.value.observations)
                .onSuccess { updated ->
                    _state.update { it.copy(
                        currentAttention = updated, 
                        isLoading = false,
                        successMessage = "Atención finalizada"
                    ) }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = "Error al finalizar: ${e.message}", isLoading = false) }
                }
        }
    }
    
    fun resetState() {
        _state.value = AttentionUiState()
    }

    fun clearError() = _state.update { it.copy(error = null) }
    fun clearSuccess() = _state.update { it.copy(successMessage = null) }
}
