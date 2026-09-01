package com.pointcheck.features.attentions.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.features.attentions.data.dto.AttentionResponseDto
import com.pointcheck.features.attentions.data.repository.AttentionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Representa el estado de la interfaz de usuario para el seguimiento de una atención en curso.
 *
 * @property currentAttention Detalles de la atención actual (si existe).
 * @property observations Notas u observaciones ingresadas por el especialista.
 * @property isLoading Indica si hay una operación asíncrona en curso.
 * @property successMessage Mensaje de éxito a mostrar.
 */
data class AttentionUiState(
    val currentAttention: AttentionResponseDto? = null,
    val observations: String = "",
    val isLoading: Boolean = false,
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

    // Canal para errores de un solo disparo (Sprint 3: Resiliencia)
    private val _errorEvents = Channel<String>()
    val errorEvents = _errorEvents.receiveAsFlow()

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
            _state.update { it.copy(isLoading = true) }
            repository.getAttentionByReservation(reservationId)
                .onSuccess { attention ->
                    _state.update { it.copy(
                        currentAttention = attention,
                        observations = attention.observations ?: "",
                        isLoading = false
                    ) }
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    if (e is com.pointcheck.core.network.ApiException && e.code in listOf(401, 403)) return@onFailure
                    _state.update { it.copy(isLoading = false) }
                    // Sprint 4 Fix: NetworkHandler envuelve el error en ApiException.
                    val is404 = (e as? com.pointcheck.core.network.ApiException)?.code == 404
                    
                    if (is404) {
                        _state.update { it.copy(currentAttention = null) }
                    } else {
                        _errorEvents.send("No se pudo cargar la atención: ${e.message}")
                    }
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
            _state.update { it.copy(isLoading = true) }
            repository.startAttention(reservationId, _state.value.observations)
                .onSuccess { attention ->
                    _state.update { it.copy(
                        currentAttention = attention, 
                        isLoading = false,
                        successMessage = "Atención iniciada"
                    ) }
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    if (e is com.pointcheck.core.network.ApiException && e.code in listOf(401, 403)) return@onFailure
                    _state.update { it.copy(isLoading = false) }
                    _errorEvents.send("Error al iniciar: ${e.message}")
                }
        }
    }

    /**
     * Finaliza la prestación del servicio.
     * Sprint 3: Refactorizado con captura proactiva de errores de negocio y Http.
     */
    fun finishAttention(duration: Int = 60) {
        val attentionId = _state.value.currentAttention?.id ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.finishAttention(
                attentionId = attentionId,
                observations = _state.value.observations,
                durationMinutes = duration
            ).onSuccess { updated ->
                _state.update { it.copy(
                    currentAttention = updated,
                    isLoading = false,
                    successMessage = "Atención finalizada y cobro generado con éxito."
                ) }
            }.onFailure { e ->
                if (e is CancellationException) throw e
                if (e is com.pointcheck.core.network.ApiException && e.code in listOf(401, 403)) return@onFailure
                
                // Sprint 4: El backend ya envía el mensaje humano gracias al ExceptionHandler basado en Map.
                // Simplemente lo propagamos.
                val errorMessage = e.message ?: "Error de conexión inesperado"

                _state.update { it.copy(isLoading = false) }
                _errorEvents.send(errorMessage)
            }
        }
    }
    
    /** Reinicia el estado de la atención a sus valores por defecto. */
    fun resetState() {
        _state.value = AttentionUiState()
    }

    /** Limpia el mensaje de éxito del estado. */
    fun clearSuccess() = _state.update { it.copy(successMessage = null) }
}
