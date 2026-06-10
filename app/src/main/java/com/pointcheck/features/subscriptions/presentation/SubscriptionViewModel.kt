package com.pointcheck.features.subscriptions.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.features.subscriptions.data.dto.SubscriptionRequestDto
import com.pointcheck.features.subscriptions.data.dto.SubscriptionResponseDto
import com.pointcheck.features.subscriptions.data.repository.SubscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Representa el estado de la interfaz de usuario para la gestión de suscripciones.
 *
 * @property professionalProfileId Identificador del perfil profesional asociado.
 * @property currentSubscription Detalles de la suscripción activa o reciente, si existe.
 * @property isLoading Indica si hay una operación de carga en curso.
 * @property error Mensaje de error a mostrar en caso de fallo.
 * @property successMessage Mensaje de éxito a mostrar tras una operación exitosa.
 * @property hasActiveSubscription Indica si el profesional tiene una suscripción con estado activo.
 */
data class SubscriptionUiState(
    val professionalProfileId: String? = null,
    val currentSubscription: SubscriptionResponseDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val hasActiveSubscription: Boolean = false
)

/**
 * ViewModel encargado de la lógica de negocio para la gestión de suscripciones de profesionales.
 * Permite cargar la suscripción actual, crear nuevas suscripciones y cancelarlas.
 *
 * @param application Contexto de la aplicación para acceder a preferencias de usuario.
 */
class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SubscriptionRepository(ApiClient.instance)
    private val prefs = UserPreferences(application)

    private val _state = MutableStateFlow(SubscriptionUiState())
    val state: StateFlow<SubscriptionUiState> = _state

    init {
        loadCurrentSubscription()
    }

    /**
     * Carga la suscripción actual vinculada al perfil profesional del usuario autenticado.
     * Si no se encuentra una suscripción activa, se maneja el estado correspondiente.
     */
    fun loadCurrentSubscription() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val profileId = prefs.professionalProfileId.first()
            
            if (profileId == null) {
                _state.update { it.copy(isLoading = false, error = "Perfil profesional no encontrado") }
                return@launch
            }

            _state.update { it.copy(professionalProfileId = profileId) }

            repository.getCurrentSubscriptionByProfessionalProfile(profileId)
                .onSuccess { sub ->
                    _state.update { it.copy(
                        currentSubscription = sub, 
                        isLoading = false,
                        hasActiveSubscription = sub.status == "ACTIVE"
                    ) }
                }
                .onFailure { e ->
                    if (e.message == "NO_SUBSCRIPTION") {
                        _state.update { it.copy(currentSubscription = null, isLoading = false, hasActiveSubscription = false) }
                    } else {
                        _state.update { it.copy(isLoading = false, error = "Error al cargar suscripción: ${e.message}") }
                    }
                }
        }
    }

    /**
     * Crea una nueva suscripción para un plan específico.
     * Calcula automáticamente las fechas de inicio y fin (30 días de duración).
     *
     * @param planName Nombre del plan de suscripción a activar (ej. "BASIC", "PREMIUM").
     */
    fun createSubscription(planName: String) {
        val profileId = _state.value.professionalProfileId ?: return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
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
                    _state.update { it.copy(
                        currentSubscription = sub, 
                        isLoading = false, 
                        hasActiveSubscription = true,
                        successMessage = "Suscripción $planName activada con éxito"
                    ) }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = "Error al activar plan: ${e.message}", isLoading = false) }
                }
        }
    }

    /**
     * Cancela la suscripción actualmente activa del profesional.
     * Actualiza el estado local tras confirmar la cancelación en el backend.
     */
    fun cancelSubscription() {
        val subId = _state.value.currentSubscription?.id ?: return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.cancelSubscription(subId)
                .onSuccess { sub ->
                    _state.update { it.copy(
                        currentSubscription = sub, 
                        isLoading = false, 
                        hasActiveSubscription = false,
                        successMessage = "Suscripción cancelada correctamente"
                    ) }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = "Error al cancelar: ${e.message}", isLoading = false) }
                }
        }
    }
    
    /** Limpia el mensaje de error del estado. */
    fun clearError() = _state.update { it.copy(error = null) }

    /** Limpia el mensaje de éxito del estado. */
    fun clearSuccess() = _state.update { it.copy(successMessage = null) }
}
