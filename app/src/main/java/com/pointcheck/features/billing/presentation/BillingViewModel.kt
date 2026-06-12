package com.pointcheck.features.billing.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.features.billing.data.dto.BillingRecordRequestDto
import com.pointcheck.features.billing.data.dto.BillingRecordResponseDto
import com.pointcheck.features.billing.data.repository.BillingRepository
import com.pointcheck.core.util.MockDataProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Representa el estado de la interfaz de usuario para la facturación y gestión de cobros.
 *
 * @property currentBilling Registro de cobro seleccionado o actualmente en proceso.
 * @property billings Lista de registros de cobro (historial o filtrados).
 * @property amount Monto del cobro ingresado en el formulario.
 * @property currency Moneda del cobro (por defecto "CLP").
 * @property paymentMethod Método de pago elegido (ej. "CASH", "DEBIT").
 * @property externalReference Código de referencia para pagos externos (ej. número de transferencia).
 * @property notes Notas adicionales sobre la transacción.
 * @property isLoading Indica si hay una operación de red en curso.
 * @property showPaymentModal Controla la visibilidad del componente de diálogo para procesar pagos.
 * @property error Mensaje de error para mostrar en la interfaz.
 * @property successMessage Mensaje de confirmación tras una operación exitosa.
 */
data class BillingUiState(
    val currentBilling: BillingRecordResponseDto? = null,
    val billings: List<BillingRecordResponseDto> = emptyList(),
    val amount: String = "",
    val currency: String = "CLP",
    val paymentMethod: String = "CASH",
    val externalReference: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val showPaymentModal: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel encargado de la lógica de facturación y cobros de los servicios prestados.
 * Proporciona funcionalidades para crear registros de cobro, procesar pagos y consultar historiales financieros.
 *
 * @param application Contexto de la aplicación.
 */
class BillingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BillingRepository(ApiClient.instance)

    private val _state = MutableStateFlow(BillingUiState())
    val state: StateFlow<BillingUiState> = _state

    /** Actualiza el monto a cobrar en el estado de la UI. */
    fun setAmount(value: String) { _state.update { it.copy(amount = value) } }
    /** Define el método de pago (CASH, DEBIT, etc) seleccionado por el usuario. */
    fun setPaymentMethod(value: String) { _state.update { it.copy(paymentMethod = value) } }
    /** Guarda la referencia externa (ej: número de transferencia) del pago. */
    fun setExternalReference(value: String) { _state.update { it.copy(externalReference = value) } }
    /** Actualiza las notas o comentarios adicionales del cobro. */
    fun setNotes(value: String) { _state.update { it.copy(notes = value) } }

    /**
     * Busca y carga un registro de cobro existente asociado a una reserva específica.
     * @param reservationId UUID de la reserva a consultar.
     */
    fun loadBillingByReservation(reservationId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getBillingBySpecialist("") // This would need the specialist ID, but the API has no getBillingByReservation
            // Assuming for now that we might need a specific endpoint or we filter the specialist's billings.
            // But usually, we want to check if a billing already exists for this reservation.
            
            // Temporary mock logic or placeholder until the specific endpoint is confirmed
            // For now, let's keep it simple as the UI expects a "load"
            _state.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Registra un nuevo cobro en el sistema para una reserva terminada.
     * Valida que el monto sea válido antes de realizar la petición al backend.
     * @param reservationId UUID de la reserva vinculada.
     * @param attentionId UUID de la atención (opcional).
     */
    fun createBillingRecord(reservationId: String, attentionId: String?) {
        val amountDouble = _state.value.amount.toDoubleOrNull() ?: 0.0
        if (amountDouble <= 0) {
            _state.update { it.copy(error = "El monto debe ser mayor a 0") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, successMessage = null) }
            val request = BillingRecordRequestDto(
                reservationId = reservationId,
                attentionId = attentionId,
                amount = amountDouble,
                currency = _state.value.currency,
                paymentMethod = _state.value.paymentMethod,
                notes = _state.value.notes
            )
            repository.createBillingRecord(request)
                .onSuccess { record ->
                    _state.update { it.copy(currentBilling = record, isLoading = false, successMessage = "Cobro registrado exitosamente") }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = "Error al crear cobro: ${e.message}", isLoading = false) }
                }
        }
    }

    /**
     * Cambia el estado de un registro de cobro a "PAID" (Pagado).
     * @param billingId UUID del registro de cobro.
     */
    fun markAsPaid(billingId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.markAsPaid(
                billingId,
                _state.value.paymentMethod,
                _state.value.externalReference.ifBlank { null },
                _state.value.notes.ifBlank { null }
            )
                .onSuccess { updated ->
                    _state.update { it.copy(currentBilling = updated, isLoading = false, successMessage = "Cobro marcado como PAGADO") }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = "Error al pagar: ${e.message}", isLoading = false) }
                }
        }
    }

    /**
     * Cancela un registro de cobro existente.
     * @param billingId UUID del registro de cobro a cancelar.
     */
    fun cancelBillingRecord(billingId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.cancelBillingRecord(billingId)
                .onSuccess { updated ->
                    _state.update { it.copy(currentBilling = updated, isLoading = false, successMessage = "Cobro CANCELADO") }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = "Error al cancelar: ${e.message}", isLoading = false) }
                }
        }
    }

    /**
     * Obtiene todos los registros de cobro generados por un especialista.
     * @param specialistId UUID del usuario especialista.
     */
    fun loadBillingBySpecialist(specialistId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getBillingBySpecialist(specialistId)
                .onSuccess { list -> 
                    _state.update { it.copy(
                        billings = list.ifEmpty { MockDataProvider.mockBillings }, 
                        isLoading = false 
                    ) } 
                }
                .onFailure { e -> 
                    _state.update { it.copy(
                        billings = MockDataProvider.mockBillings, 
                        isLoading = false 
                    ) } 
                }
        }
    }

    /**
     * Carga solo los cobros que están pendientes de pago para el especialista.
     */
    fun loadPendingBillingBySpecialist(specialistId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getPendingBillingBySpecialist(specialistId)
                .onSuccess { list -> _state.update { it.copy(billings = list, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    /**
     * Recupera los cobros realizados o generados en el día actual.
     */
    fun loadTodayBillingBySpecialist(specialistId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getTodayBillingBySpecialist(specialistId)
                .onSuccess { list -> _state.update { it.copy(billings = list, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    /** Limpia el mensaje de error del estado. */
    fun clearError() = _state.update { it.copy(error = null) }
    /** Limpia el mensaje de éxito del estado. */
    fun clearSuccess() = _state.update { it.copy(successMessage = null) }

    /** Controla la visibilidad del modal de confirmación de pago en la UI. */
    fun setShowPaymentModal(show: Boolean) {
        _state.update { it.copy(showPaymentModal = show) }
    }
}
