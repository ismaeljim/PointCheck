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
import com.pointcheck.features.external.data.dto.WeatherResponseDto
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class BookingUiState(
    val professionals: List<SpecialistResponseDto> = emptyList(),
    val services: List<ServiceResponseDto> = emptyList(),
    val selectedProfessional: SpecialistResponseDto? = null,
    val selectedService: ServiceResponseDto? = null,
    val weather: WeatherResponseDto? = null,
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
        loadMyReservations()
    }

    fun loadProfessionals(categoryId: Long? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getActiveProfiles(categoryId)
                .onSuccess { list -> 
                    _state.update { it.copy(professionals = list, isLoading = false) } 
                }
                .onFailure { e -> 
                    _state.update { it.copy(error = e.message, isLoading = false) } 
                }
        }
    }

    fun selectProfessional(profile: SpecialistResponseDto) {
        _state.update { it.copy(
            selectedProfessional = profile, 
            selectedService = null, 
            services = emptyList(),
            weather = null,
            isValid = false
        ) }
        loadServicesForProfessional(profile.id)
    }

    fun selectProfessionalById(id: Long, categoryId: Long? = null) {
        viewModelScope.launch {
            // Asegurarse de que los profesionales estén cargados para esa categoría
            loadProfessionals(categoryId)
            
            // Esperar a que se carguen (en un caso real usaríamos un Flow o State, 
            // aquí simplificamos buscando en la lista actualizada)
            val prof = _state.value.professionals.find { it.id == id }
            if (prof != null) {
                selectProfessional(prof)
            }
        }
    }

    private fun loadWeather(city: String?) {
        if (city.isNullOrBlank()) return
        viewModelScope.launch {
            repository.getWeather(city)
                .onSuccess { weather ->
                    _state.update { it.copy(weather = weather) }
                }
        }
    }

    private fun loadServicesForProfessional(profileId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getServices(profileId)
                .onSuccess { list -> 
                    _state.update { it.copy(services = list, isLoading = false) } 
                }
                .onFailure { e -> 
                    _state.update { it.copy(error = e.message, isLoading = false) } 
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
            // Usar la zona horaria local para el cálculo de "ahora"
            val now = Calendar.getInstance(TimeZone.getDefault())
            val calendar = Calendar.getInstance(TimeZone.getDefault())
            calendar.timeInMillis = millis
            
            if (calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
                
                // Si es hoy, ponemos la hora actual + 1
                calendar.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY) + 1)
                calendar.set(Calendar.MINUTE, 0)
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, 9)
                calendar.set(Calendar.MINUTE, 0)
            }

            val newState = s.copy(reservationStartMillis = calendar.timeInMillis)
            newState.copy(isValid = validate(newState))
        }
        // Trigger weather lookup when date is selected
        _state.value.selectedProfessional?.city?.let { city ->
            loadWeather(city)
        }
    }

    fun updateReservationTime(hour: Int, minute: Int) {
        _state.update { s ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = s.reservationStartMillis ?: System.currentTimeMillis()
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            
            val newState = s.copy(reservationStartMillis = calendar.timeInMillis)
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

    fun loadMyReservations(type: String = "all") {
        viewModelScope.launch {
            val userId = prefs.userId.first() ?: return@launch
            val role = prefs.role.first() ?: "CLIENT"

            _state.update { it.copy(isLoading = true, error = null) }

            val result = if (role == "SPECIALIST" || role == "PROFESSIONAL") {
                val profileId = prefs.professionalProfileId.first()
                if (profileId != null) {
                    repository.getReservationsBySpecialist(profileId)
                } else {
                    Result.success(emptyList())
                }
            } else {
                when (type) {
                    "upcoming" -> repository.getUpcomingReservationsByClient(userId)
                    else -> repository.getReservationsByClient(userId)
                }
            }

            result.onSuccess { list ->
                _reservations.value = list.filter { it.status != "CANCELLED" }
                _state.update { it.copy(isLoading = false) }
            }.onFailure { e ->
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun createReservation(onSuccess: () -> Unit) {
        val s = _state.value
        if (!s.isValid) return

        viewModelScope.launch {
            val clientId = prefs.userId.first() ?: return@launch
            _state.update { it.copy(isLoading = true) }

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = s.reservationStartMillis!!
            
            // Usamos un formato que el backend entienda sin ambigüedades de zona horaria
            // y nos aseguramos de usar la fecha que el usuario seleccionó
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val isoStart = sdf.format(calendar.time)
            
            val duration = s.selectedService?.durationMinutes ?: 60
            val endCalendar = calendar.clone() as Calendar
            endCalendar.add(Calendar.MINUTE, duration)
            val isoEnd = sdf.format(endCalendar.time)

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
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun cancelReservation(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.cancelReservation(id)
                .onSuccess {
                    _state.update { it.copy(successMessage = "Reserva cancelada") }
                    loadMyReservations()
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
    fun clearSuccess() = _state.update { it.copy(successMessage = null) }
}
