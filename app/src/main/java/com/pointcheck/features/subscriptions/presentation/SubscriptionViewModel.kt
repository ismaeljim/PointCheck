package com.pointcheck.features.subscriptions.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.features.subscriptions.data.dto.SubscriptionRequestDto
import com.pointcheck.features.subscriptions.data.dto.SubscriptionResponseDto
import com.pointcheck.features.subscriptions.data.repository.SubscriptionRepository
import com.pointcheck.core.network.ApiException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Jerarquía de estados para la gestión de suscripciones.
 */
sealed class SubscriptionUiState {
    object Loading : SubscriptionUiState()
    data class Success(
        val professionalProfileId: String? = null,
        val currentSubscription: SubscriptionResponseDto? = null,
        val hasActiveSubscription: Boolean = false,
        val successMessage: String? = null
    ) : SubscriptionUiState()
    data class Error(val message: String) : SubscriptionUiState()
}

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SubscriptionRepository(ApiClient.instance)
    private val prefs = UserPreferences(application)

    private val _state = MutableStateFlow<SubscriptionUiState>(SubscriptionUiState.Loading)
    val state: StateFlow<SubscriptionUiState> = _state

    init {
        loadCurrentSubscription()
    }

    private fun updateSuccessState(updater: (SubscriptionUiState.Success) -> SubscriptionUiState.Success) {
        val current = _state.value
        if (current is SubscriptionUiState.Success) {
            _state.value = updater(current)
        }
    }

    fun loadCurrentSubscription() {
        viewModelScope.launch {
            _state.value = SubscriptionUiState.Loading
            val profileId = prefs.professionalProfileId.first()
            
            if (profileId == null) {
                _state.value = SubscriptionUiState.Error("Perfil profesional no encontrado")
                return@launch
            }

            repository.getCurrentSubscriptionByProfessionalProfile(profileId)
                .onSuccess { sub ->
                    _state.value = SubscriptionUiState.Success(
                        professionalProfileId = profileId,
                        currentSubscription = sub, 
                        hasActiveSubscription = sub.status == "ACTIVE"
                    )
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    if (e is ApiException && (e.code == 401 || e.code == 403)) return@onFailure
                    if (e.message == "NO_SUBSCRIPTION") {
                        _state.value = SubscriptionUiState.Success(
                            professionalProfileId = profileId,
                            currentSubscription = null, 
                            hasActiveSubscription = false
                        )
                    } else {
                        _state.value = SubscriptionUiState.Error("Error al cargar suscripción: ${e.message}")
                    }
                }
        }
    }

    fun createSubscription(planName: String) {
        val current = _state.value as? SubscriptionUiState.Success ?: return
        val profileId = current.professionalProfileId ?: return
        
        viewModelScope.launch {
            _state.value = SubscriptionUiState.Loading
            
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val now = Calendar.getInstance()
            val startDate = sdf.format(now.time)
            
            now.add(Calendar.DAY_OF_MONTH, 30)
            val endDate = sdf.format(now.time)

            val request = SubscriptionRequestDto(
                professionalProfileId = profileId,
                planName = planName,
                startDate = startDate,
                endDate = endDate
            )

            repository.createSubscription(request)
                .onSuccess { sub ->
                    _state.value = SubscriptionUiState.Success(
                        professionalProfileId = profileId,
                        currentSubscription = sub, 
                        hasActiveSubscription = true,
                        successMessage = "Suscripción $planName activada con éxito"
                    )
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    if (e is ApiException && (e.code == 401 || e.code == 403)) return@onFailure
                    _state.value = SubscriptionUiState.Error("Error al activar plan: ${e.message}")
                }
        }
    }

    fun cancelSubscription() {
        val current = _state.value as? SubscriptionUiState.Success ?: return
        val subId = current.currentSubscription?.id ?: return
        
        viewModelScope.launch {
            _state.value = SubscriptionUiState.Loading
            repository.cancelSubscription(subId)
                .onSuccess { sub ->
                    _state.value = SubscriptionUiState.Success(
                        professionalProfileId = current.professionalProfileId,
                        currentSubscription = sub, 
                        hasActiveSubscription = false,
                        successMessage = "Suscripción cancelada correctamente"
                    )
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    if (e is ApiException && (e.code == 401 || e.code == 403)) return@onFailure
                    _state.value = SubscriptionUiState.Error("Error al cancelar: ${e.message}")
                }
        }
    }
    
    fun clearError() {
        if (_state.value is SubscriptionUiState.Error) {
            loadCurrentSubscription()
        }
    }

    fun clearSuccess() {
        updateSuccessState { it.copy(successMessage = null) }
    }
}
