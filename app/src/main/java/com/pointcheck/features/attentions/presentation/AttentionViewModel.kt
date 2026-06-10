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
 * Representa el estado de la interfaz de usuario para el seguimiento de una atención en curso.
 *
 * @property currentAttention Detalles de la atención actual (si existe).
 * @property observations Notas u observaciones ingresadas por el especialista.
 * @property isLoading Indica si hay una operación asíncrona en curso.
 * @property error Mensaje de error a mostrar.
 * @property successMessage Mensaje de éxito a mostrar.
 */
data class AttentionUiState(
    val currentAttention: AttentionResponseDto? = null,
    val observations: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel encargado de gestionar el ciclo de vida de una atención técnica o médica.
 * Permite al especialista iniciar la sesión, registrar observaciones en tiempo real y finalizar el servicio,
 * lo que habitualmente desencadena procesos de facturación.
 *
 * @param application Contexto de la aplicación.
 */
class AttentionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AttentionRepository(ApiClient.instance)

    private val _state = MutableStateFlow(AttentionUiState())
    val state: StateFlow<AttentionUiState> = _state

    /**
     * Actualiza las observaciones locales del especialista.
     *
     * @param value Texto de la observación.
     */
    fun setObservations(value: String) {
        _state.update { it.copy(observations = value) }
    }

    /**
     * Carga la atención asociada a una reserva específica si ya existe en el sistema.
     *
     * @param reservationId ID de la reserva.
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
     * Gatilla el inicio oficial de la prestación del servicio.
     * Actualiza el estado de la reserva y crea un registro de atención.
     *
     * @param reservationId ID de la reserva vinculada.
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
     * Finaliza la prestación del servicio.
     * Persiste las observaciones finales y cambia el estado de la reserva a 'COMPLETED'.
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
    
    /** Reinicia el estado de la atención a sus valores por defecto. */
    fun resetState() {
        _state.value = AttentionUiState()
    }

    /** Limpia el mensaje de error del estado. */
    fun clearError() = _state.update { it.copy(error = null) }
    
    /** Limpia el mensaje de éxito del estado. */
    fun clearSuccess() = _state.update { it.copy(successMessage = null) }
}
