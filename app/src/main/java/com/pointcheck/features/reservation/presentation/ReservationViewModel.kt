package com.pointcheck.features.reservation.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.features.reservation.data.dto.*
import com.pointcheck.features.reservation.data.repository.ReservationRepository
import com.pointcheck.features.services.data.dto.ServiceResponseDto
import com.pointcheck.features.profile.data.dto.ProfessionalProfileResponseDto
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class BookingUiState(
    val professionals: List<ProfessionalProfileResponseDto> = emptyList(),
    val services: List<ServiceResponseDto> = emptyList(),
    val selectedProfessional: ProfessionalProfileResponseDto? = null,
    val selectedService: ServiceResponseDto? = null,
    val reservationStartMillis: Long? = null,
    val notes: String = "",
    val isLoading: Boolean = false,
    val isValid: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class ReservationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ReservationRepository(ApiClient.instance)
    private val prefs = UserPreferences(application)

    private val _state = MutableStateFlow(BookingUiState())
    val state: StateFlow<BookingUiState> = _state

    private val _reservations = MutableStateFlow<List<ReservationResponseDto>>(emptyList())
    val reservations: StateFlow<List<ReservationResponseDto>> = _reservations

    init {
        loadProfessionals()
        loadMyReservations()
    }

    fun loadProfessionals() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getActiveProfiles()
                .onSuccess { list -> 
                    _state.update { it.copy(professionals = list, isLoading = false) } 
                }
                .onFailure { e -> 
                    _state.update { it.copy(error = "Error al cargar profesionales", isLoading = false) } 
                }
        }
    }

    fun selectProfessional(profile: ProfessionalProfileResponseDto) {
        _state.update { it.copy(
            selectedProfessional = profile, 
            selectedService = null, 
            services = emptyList(),
            isValid = false
        ) }
        loadServicesForProfessional(profile.id)
    }

    private fun loadServicesForProfessional(profileId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getServices(profileId)
                .onSuccess { list -> 
                    _state.update { it.copy(services = list, isLoading = false) } 
                }
                .onFailure { 
                    _state.update { it.copy(error = "Error al cargar servicios", isLoading = false) } 
                }
        }
    }

    fun selectService(service: ServiceResponseDto) {
        _state.update { s -> 
            val newState = s.copy(selectedService = service)
            newState.copy(isValid = validate(newState))
        }
    }

    fun setReservationDateTime(millis: Long) {
        _state.update { s ->
            val newState = s.copy(reservationStartMillis = millis)
            newState.copy(isValid = validate(newState))
        }
    }

    fun setNotes(notes: String) {
        _state.update { it.copy(notes = notes) }
    }

    private fun validate(s: BookingUiState): Boolean {
        return s.selectedProfessional != null && 
               s.selectedService != null && 
               s.reservationStartMillis != null &&
               s.reservationStartMillis > System.currentTimeMillis()
    }

    fun loadMyReservations() {
        viewModelScope.launch {
            val userId = prefs.userId.first() ?: return@launch
            val role = prefs.role.first() ?: "CLIENT"

            _state.update { it.copy(isLoading = true) }

            val result = if (role == "SPECIALIST" || role == "PROFESSIONAL") {
                val profileId = prefs.professionalProfileId.first()
                if (profileId != null) {
                    repository.getReservationsBySpecialist(profileId)
                } else {
                    Result.success(emptyList())
                }
            } else {
                repository.getUpcomingReservationsByClient(userId)
            }

            result.onSuccess { list ->
                _reservations.value = list.filter { it.status != "CANCELLED" }
                _state.update { it.copy(isLoading = false) }
            }.onFailure {
                _state.update { it.copy(error = "Error al cargar reservas", isLoading = false) }
            }
        }
    }

    fun createReservation(onSuccess: () -> Unit) {
        val s = _state.value
        if (!s.isValid) return

        viewModelScope.launch {
            val clientId = prefs.userId.first() ?: return@launch
            _state.update { it.copy(isLoading = true) }

            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val startDate = Date(s.reservationStartMillis!!)
            val isoStart = sdf.format(startDate)
            
            // Calcular fin (60 min por defecto o duración del servicio)
            val duration = s.selectedService?.durationMinutes ?: 60
            val endDate = Date(s.reservationStartMillis + (duration * 60 * 1000))
            val isoEnd = sdf.format(endDate)

            val request = ReservationRequestDto(
                clientId = clientId,
                specialistId = s.selectedProfessional!!.userId,
                serviceId = s.selectedService!!.id,
                reservationStart = isoStart,
                reservationEnd = isoEnd,
                notes = s.notes.ifBlank { null }
            )

            repository.createReservation(request)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, successMessage = "Reserva creada con éxito") }
                    loadMyReservations()
                    onSuccess()
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = "Error: ${e.message}") }
                }
        }
    }

    fun cancelReservation(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.cancelReservation(id)
                .onSuccess {
                    loadMyReservations()
                    _state.update { it.copy(isLoading = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = "Error al cancelar: ${e.message}", isLoading = false) }
                }
        }
    }
}
