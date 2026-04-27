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
    val record: BillingRecordResponseDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class BillingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BillingRepository(ApiClient.instance)

    private val _state = MutableStateFlow(BillingUiState())
    val state: StateFlow<BillingUiState> = _state

    fun loadBillingByReservation(reservationId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getBillingByReservation(reservationId)
                .onSuccess { record ->
                    _state.update { it.copy(record = record, isLoading = false) }
                }
                .onFailure {
                    _state.update { it.copy(isLoading = false) }
                }
        }
    }

    fun createBilling(request: BillingRecordRequestDto) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.createBillingRecord(request)
                .onSuccess { record ->
                    _state.update { it.copy(record = record, isLoading = false, isSuccess = true) }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = "Error al registrar cobro: ${e.message}", isLoading = false) }
                }
        }
    }

    fun updateStatus(id: Long, status: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.updateBillingStatus(id, status)
                .onSuccess { updated ->
                    _state.update { it.copy(record = updated, isLoading = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = "Error al actualizar estado: ${e.message}", isLoading = false) }
                }
        }
    }
}
