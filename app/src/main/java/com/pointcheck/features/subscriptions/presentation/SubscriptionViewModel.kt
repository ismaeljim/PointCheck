package com.pointcheck.features.subscriptions.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.features.subscriptions.data.dto.SubscriptionRequestDto
import com.pointcheck.features.subscriptions.data.dto.SubscriptionResponseDto
import com.pointcheck.features.subscriptions.data.repository.SubscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SubscriptionUiState(
    val subscription: SubscriptionResponseDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SubscriptionRepository(ApiClient.instance)

    private val _state = MutableStateFlow(SubscriptionUiState())
    val state: StateFlow<SubscriptionUiState> = _state

    fun loadSubscription(profileId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getSubscriptionByProfileId(profileId)
                .onSuccess { sub ->
                    _state.update { it.copy(subscription = sub, isLoading = false) }
                }
                .onFailure {
                    _state.update { it.copy(isLoading = false) }
                }
        }
    }

    fun upgradePlan(profileId: Long, planName: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.createSubscription(SubscriptionRequestDto(profileId, planName))
                .onSuccess { sub ->
                    _state.update { it.copy(subscription = sub, isLoading = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = "Error al actualizar plan: ${e.message}", isLoading = false) }
                }
        }
    }
}
