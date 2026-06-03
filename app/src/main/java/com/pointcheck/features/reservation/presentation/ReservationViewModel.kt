package com.pointcheck.features.reservation.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.features.external.data.dto.WeatherResponseDto
import com.pointcheck.features.reservation.data.dto.ReservationRequestDto
import com.pointcheck.features.reservation.data.dto.ReservationResponseDto
import com.pointcheck.features.services.data.dto.ServiceResponseDto
import com.pointcheck.features.reservation.data.dto.SpecialistResponseDto
import com.pointcheck.features.reservation.data.repository.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * AUDITORÍA TÉCNICA: Gestión de Reservas (UI State Management)
 * 
 * Este ViewModel orquestra el flujo multipaso de reserva:
 * Selección Especialista -> Servicio -> Fecha/Hora -> Confirmación.
 * 
 * Hallazgos:
 * 1. [OK] Reactividad: Uso de StateFlow para manejar el estado de la UI (BookingUiState).
 * 2. [OK] Validación Centralizada: Método 'validate' garantiza integridad antes de habilitar el botón de reserva.
 * 3. [MEJORA] Inyección de Dependencias: El repositorio se instancia directamente; se recomienda Hilt/Koin.
 * 4. [OK] UX - Feedback: Manejo de estados de carga (isLoading) y mensajes de éxito/error.
 */
data class BookingUiState(
    val professionals: List<SpecialistResponseDto> = emptyList(),
    val services: List<ServiceResponseDto> = emptyList(),
    val selectedProfessional: SpecialistResponseDto? = null,
    val selectedService: ServiceResponseDto? = null,
    val weather: WeatherResponseDto? = null,
    val reservationStartMillis: Long? = null,
    val availableSlots: List<String> = emptyList(),
    val selectedSlot: String? = null,
    val paymentMethod: String? = null,
    val notes: String = "",
    val isLoading: Boolean = false,
    val isAvailabilityLoading: Boolean = false,
    val isValid: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class ReservationViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = ReservationRepository(ApiClient.instance)
    private val prefs = UserPreferences(app)

    private val _state = MutableStateFlow(BookingUiState())
    val state: StateFlow<BookingUiState> = _state

    private val _reservations = MutableStateFlow<List<ReservationResponseDto>>(emptyList())
    val reservations: StateFlow<List<ReservationResponseDto>> = _reservations

    init {
        loadProfessionals()
    }

    fun loadProfessionals(categoryId: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getActiveProfiles(categoryId)
                .onSuccess { list ->
                    _state.update { it.copy(professionals = list, isLoading = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    /**
     * AUDITORÍA: Selección de Especialista.
     * Gatilla carga asíncrona de servicios y clima local para mejorar la UX.
     */
    fun selectProfessional(professional: SpecialistResponseDto) {
        _state.update { it.copy(
            selectedProfessional = professional,
            selectedService = null,
            availableSlots = emptyList(),
            selectedSlot = null
        ) }
        loadServicesForProfessional(professional.id)
        loadWeather(professional.city)
        loadAvailabilityIfPossible()
    }

    fun selectProfessionalById(id: String?, categoryId: String? = null) {
        if (id.isNullOrBlank() || id == "null") {
            loadProfessionals(categoryId)
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val currentList = _state.value.professionals
            var found = currentList.find { 
                it.id.trim().equals(id.trim(), ignoreCase = true) || 
                it.userId.trim().equals(id.trim(), ignoreCase = true) 
            }

            if (found == null) {
                repository.getActiveProfiles(null) 
                    .onSuccess { list ->
                        found = list.find { 
                            it.id.trim().equals(id.trim(), ignoreCase = true) || 
                            it.userId.trim().equals(id.trim(), ignoreCase = true) 
                        }
                        
                        _state.update { s -> s.copy(
                            professionals = list,
                            selectedProfessional = found,
                            isLoading = false
                        ).let { it.copy(isValid = validate(it)) } }
                        
                        val finalFound = found
                        if (finalFound != null) {
                            loadServicesForProfessional(finalFound.id)
                            loadWeather(finalFound.city)
                            loadAvailabilityIfPossible()
                        }
                    }
                    .onFailure { e ->
                        _state.update { it.copy(error = e.message, isLoading = false) }
                    }
            } else {
                _state.update { s -> s.copy(
                    selectedProfessional = found,
                    isLoading = false
                ).let { it.copy(isValid = validate(it)) } }
                
                val finalFound = found
                if (finalFound != null) {
                    loadServicesForProfessional(finalFound.id)
                    loadWeather(finalFound.city)
                    loadAvailabilityIfPossible()
                }
            }
        }
    }

    private fun loadWeather(city: String?) {
        if (city == null) return
        viewModelScope.launch {
            repository.getWeather(city)
                .onSuccess { w -> _state.update { it.copy(weather = w) } }
        }
    }

    private fun loadServicesForProfessional(professionalProfileId: String) {
        viewModelScope.launch {
            repository.getServices(professionalProfileId)
                .onSuccess { list ->
                    _state.update { it.copy(services = list) }
                }
        }
    }

    fun selectService(service: ServiceResponseDto) {
        _state.update { s -> s.copy(selectedService = service).let { it.copy(isValid = validate(it)) } }
        loadAvailabilityIfPossible()
    }

    /**
     * AUDITORÍA: Gestión de Tiempo.
     * Al seleccionar fecha, se limpian slots previos y se gatilla consulta de disponibilidad.
     */
    fun setReservationDateTime(millis: Long) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 9)
        cal.set(Calendar.MINUTE, 0)
        
        _state.update { s -> 
            s.copy(reservationStartMillis = cal.timeInMillis, selectedSlot = null)
                .let { it.copy(isValid = validate(it)) } 
        }
        loadAvailabilityIfPossible()
    }

    private fun loadAvailabilityIfPossible() {
        val s = _state.value
        if (s.selectedProfessional != null && s.reservationStartMillis != null) {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(s.reservationStartMillis)
            viewModelScope.launch {
                _state.update { it.copy(isAvailabilityLoading = true) }
                repository.getAvailability(s.selectedProfessional.id, dateStr)
                    .onSuccess { resp ->
                        _state.update { it.copy(availableSlots = resp.availableSlots, isAvailabilityLoading = false) }
                    }
                    .onFailure { e ->
                        _state.update { it.copy(error = e.message, isAvailabilityLoading = false, availableSlots = emptyList()) }
                    }
            }
        }
    }

    fun updateReservationTimeFromSlot(slot: String) {
        val s = _state.value
        val currentMillis = s.reservationStartMillis ?: return
        
        val cal = Calendar.getInstance()
        cal.timeInMillis = currentMillis
        val parts = slot.split(":")
        if (parts.size == 2) {
            cal.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
            cal.set(Calendar.MINUTE, parts[1].toInt())
        }
        
        _state.update { it.copy(
            reservationStartMillis = cal.timeInMillis,
            selectedSlot = slot
        ).let { it.copy(isValid = validate(it)) } }
    }

    fun setNotes(notes: String) {
        _state.update { it.copy(notes = notes) }
    }

    fun setPaymentMethod(method: String) {
        _state.update { s -> s.copy(paymentMethod = method).let { it.copy(isValid = validate(it)) } }
    }

    /**
     * AUDITORÍA: Motor de Validación.
     * Requisito: Especialista + Servicio + Fecha + Hora (o unidad diaria) + Método Pago.
     */
    private fun validate(s: BookingUiState): Boolean {
        val isDayUnit = s.selectedService?.priceUnit == "DAY"
        return s.selectedProfessional != null &&
               s.selectedService != null &&
               s.reservationStartMillis != null &&
               s.paymentMethod != null &&
               (s.selectedSlot != null || isDayUnit)
    }

    fun loadMyReservations(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getReservationsByClient(userId)
                .onSuccess { list ->
                    _reservations.value = list
                    _state.update { it.copy(isLoading = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    fun createReservation(onDone: () -> Unit) {
        val s = _state.value
        if (!validate(s)) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val userId = prefs.userId.first() ?: return@launch
            
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val startStr = sdf.format(s.reservationStartMillis)
            
            val request = ReservationRequestDto(
                clientId = userId,
                specialistId = s.selectedProfessional!!.id,
                serviceId = s.selectedService!!.id,
                reservationStart = startStr,
                notes = s.notes,
                paymentMethod = s.paymentMethod
            )

            repository.createReservation(request)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, successMessage = "Reserva creada con éxito") }
                    onDone()
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun cancelReservation(reservationId: String) {
        viewModelScope.launch {
            repository.cancelReservation(reservationId)
                .onSuccess {
                    val userId = prefs.userId.first()
                    if (userId != null) loadMyReservations(userId)
                    _state.update { it.copy(successMessage = "Reserva cancelada") }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message) }
                }
        }
    }

    fun confirmCashPayment(reservationId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.updateReservationStatus(reservationId, "COMPLETED")
                .onSuccess {
                    _state.update { it.copy(isLoading = false, successMessage = "Pago en efectivo confirmado") }
                    val userId = prefs.userId.first()
                    if (userId != null) loadMyReservations(userId)
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = "Error al confirmar: ${e.message}") }
                }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
    fun clearSuccess() = _state.update { it.copy(successMessage = null) }
}
