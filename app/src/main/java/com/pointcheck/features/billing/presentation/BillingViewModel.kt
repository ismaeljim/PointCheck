package com.pointcheck.features.billing.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.features.billing.data.dto.BillingRecordRequestDto
import com.pointcheck.features.billing.data.dto.BillingRecordResponseDto
import com.pointcheck.features.billing.data.repository.BillingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BillingUiState(
    val currentBilling: BillingRecordResponseDto? = null,
    val billings: List<BillingRecordResponseDto> = emptyList(),
    val amount: String = "",
    val currency: String = "CLP",
    val paymentMethod: String = "CASH",
    val externalReference: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class BillingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BillingRepository(ApiClient.instance)

    private val _state = MutableStateFlow(BillingUiState())
    val state: StateFlow<BillingUiState> = _state

    fun setAmount(value: String) { _state.update { it.copy(amount = value) } }
    fun setPaymentMethod(value: String) { _state.update { it.copy(paymentMethod = value) } }
    fun setExternalReference(value: String) { _state.update { it.copy(externalReference = value) } }
    fun setNotes(value: String) { _state.update { it.copy(notes = value) } }

    fun createBillingRecord(reservationId: Long, attentionId: Long?) {
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

    fun markAsPaid(billingId: Long) {
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

    fun cancelBillingRecord(billingId: Long) {
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

    fun loadBillingBySpecialist(specialistId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getBillingBySpecialist(specialistId)
                .onSuccess { list -> _state.update { it.copy(billings = list, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    fun loadPendingBillingBySpecialist(specialistId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getPendingBillingBySpecialist(specialistId)
                .onSuccess { list -> _state.update { it.copy(billings = list, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    fun loadTodayBillingBySpecialist(specialistId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getTodayBillingBySpecialist(specialistId)
                .onSuccess { list -> _state.update { it.copy(billings = list, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
        }
    }
}
