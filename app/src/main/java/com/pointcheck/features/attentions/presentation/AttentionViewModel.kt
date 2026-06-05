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

/**
 * AUDITORÍA TÉCNICA: Gestión de la Sesión de Atención (Especialista)
 * 
 * Este ViewModel controla el estado de la prestación del servicio en tiempo real.
 * Permite al especialista iniciar la sesión, registrar observaciones y finalizarla.
 * 
 * Hallazgos:
 * 1. [OK] Ciclo de Vida: Manejo claro de estados 'Start' y 'Finish'.
 * 2. [OK] Persistencia de Observaciones: Las notas se actualizan reactivamente en el UI State.
 * 3. [INFO] Integración Backend: El cierre de atención gatilla automáticamente la facturación en el servidor.
 * 4. [MEJORA] Temporizador: Se podría agregar un cronómetro visual para que el especialista vea la duración real.
 */
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

    /**
     * Carga la atención asociada a una reserva si ya existe.
     */
    fun loadAttentionByReservation(reservationId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getAttentionsByReservation(reservationId)
                .onSuccess { attentions ->
                    val lastAttention = attentions.lastOrNull()
                    _state.update { it.copy(
                        currentAttention = lastAttention,
                        observations = lastAttention?.observations ?: "",
                        isLoading = false
                    ) }
                }
                .onFailure { e ->
                    // Si no hay atención aún, no es necesariamente un error crítico para el UI
                    _state.update { it.copy(isLoading = false) }
                }
        }
    }

    /**
     * AUDITORÍA: Gatilla el inicio de la atención en el backend.
     * La reserva cambia a estado 'CONFIRMED'.
     */
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

    /**
     * AUDITORÍA: Gatilla el fin de la atención.
     * La reserva cambia a 'COMPLETED' y se genera el cobro (BillingRecord).
     */
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
