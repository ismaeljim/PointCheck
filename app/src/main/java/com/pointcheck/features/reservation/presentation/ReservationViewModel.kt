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
import com.pointcheck.core.notifications.ReminderScheduler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Representa el estado de la interfaz de usuario para el proceso de reserva.
 *
 * @property professionals Lista de especialistas disponibles.
 * @property services Lista de servicios ofrecidos por el profesional seleccionado.
 * @property selectedProfessional Especialista seleccionado para la reserva.
 * @property selectedService Servicio seleccionado para la reserva.
 * @property weather Información climática de la ciudad del profesional.
 * @property reservationStartMillis Fecha y hora de inicio de la reserva en milisegundos.
 * @property availableSlots Bloques horarios disponibles para la fecha seleccionada.
 * @property selectedSlot Bloque horario seleccionado (ej. "09:00").
 * @property paymentMethod Método de pago elegido (ej. "CASH", "TRANSFER").
 * @property notes Notas adicionales o dirección para servicios a domicilio.
 * @property isLoading Indica si se está realizando una operación de carga general.
 * @property isAvailabilityLoading Indica si se está consultando la disponibilidad horaria.
 * @property isValid Indica si todos los campos requeridos para la reserva están completos.
 * @property error Mensaje de error a mostrar en la interfaz.
 * @property successMessage Mensaje de éxito tras una operación exitosa.
 */
/**
 * Jerarquía de estados para el proceso de reserva y gestión de agenda.
 * Garantiza transiciones de UI consistentes y atómicas.
 */
sealed class BookingUiState {
    object Loading : BookingUiState()
    
    data class Success(
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
        val searchQuery: String = "",
        val filteredProfessionals: List<SpecialistResponseDto> = emptyList(),
        val isAvailabilityLoading: Boolean = false,
        val isAtHomeAddressMissing: Boolean = false,
        val isValid: Boolean = false,
        val successMessage: String? = null
    ) : BookingUiState()

    data class Error(val message: String) : BookingUiState()
}

/**
 * ViewModel encargado de orquestar el flujo de reserva de citas.
 * ... (rest of the class remains similar but adapted to sealed state)
 */
class ReservationViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = ReservationRepository(ApiClient.instance)
    private val prefs = UserPreferences(app)
    private val scheduler = ReminderScheduler(app)

    private val _state = MutableStateFlow<BookingUiState>(BookingUiState.Loading)
    val state: StateFlow<BookingUiState> = _state

    private val _reservations = MutableStateFlow<List<ReservationResponseDto>>(emptyList())
    val reservations: StateFlow<List<ReservationResponseDto>> = _reservations

    private val _attentions = MutableStateFlow<List<ReservationResponseDto>>(emptyList())
    val attentions: StateFlow<List<ReservationResponseDto>> = _attentions

    private var dualAgendaJob: Job? = null
    private var loadProsJob: Job? = null
    private var availabilityJob: Job? = null

    init {
        loadProfessionals()
        observeUserAddress()
    }

    private var userAddress: String? = null

    private fun observeUserAddress() {
        viewModelScope.launch {
            prefs.address.collect { address ->
                userAddress = address
                updateState { it.copy(isValid = validate(it)) }
            }
        }
    }

    private fun updateState(updater: (BookingUiState.Success) -> BookingUiState.Success) {
        val current = _state.value
        if (current is BookingUiState.Success) {
            _state.value = updater(current)
        }
    }

    /**
     * Carga tanto las reservas del cliente como las atenciones del especialista para un usuario.
     */
    fun loadDualAgenda(userId: String) {
        dualAgendaJob?.cancel()
        dualAgendaJob = viewModelScope.launch {
            _state.value = BookingUiState.Loading
            
            val role = prefs.role.first() ?: "CLIENT"

            try {
                if (role == "ADMIN") {
                    repository.getAllReservations()
                        .onSuccess { list ->
                            _reservations.value = list
                            _attentions.value = emptyList()
                        }
                        .onFailure { throw it }
                } else {
                    val resResult = repository.getReservationsByClient(userId)
                    
                    val profileResult = ApiClient.instance.getProfessionalProfileByUserId(userId)
                    val attResult = if (profileResult.isSuccessful && profileResult.body() != null) {
                        repository.getReservationsBySpecialist(profileResult.body()!!.id)
                    } else {
                        Result.success(emptyList())
                    }

                    resResult.onSuccess { _reservations.value = it }.onFailure { if (it is CancellationException) throw it else throw it }
                    attResult.onSuccess { _attentions.value = it }.onFailure { if (it is CancellationException) throw it else throw it }
                }
                
                val currentState = _state.value
                if (currentState is BookingUiState.Success) {
                    _state.value = currentState.copy(successMessage = null)
                } else {
                    _state.value = BookingUiState.Success()
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                // SPRINT 4: Solo forzamos re-login si el error es de identidad real (401/403).
                // Ahora usamos ApiException para detectar el código exacto de forma segura.
                val code = when (e) {
                    is com.pointcheck.core.network.ApiException -> e.code
                    is retrofit2.HttpException -> e.code()
                    else -> 0
                }
                
                if (code == 401 || code == 403) {
                    return@launch
                } else {
                    _state.value = BookingUiState.Error(e.localizedMessage ?: "Error al cargar agenda")
                }
            }
        }
    }



    fun confirmPayment(reservationId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            val userId = prefs.userId.first() ?: return@launch
            _state.value = BookingUiState.Loading
            
            repository.confirmPayment(userId, reservationId)
                .onSuccess {
                    loadDualAgenda(userId)
                    loadMyReservations(userId)
                    _state.value = BookingUiState.Success(successMessage = "Cita completada y pago registrado con éxito")
                    onDone()
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    // Mostramos el mensaje del backend directamente para que sea intuitivo
                    _state.value = BookingUiState.Error(e.message ?: "No se pudo procesar el pago en este momento")
                }
        }
    }

    fun clearError() {
        if (_state.value is BookingUiState.Error) {
            _state.value = BookingUiState.Success()
        }
    }

    fun loadProfessionals(categoryId: String? = null) {
        loadProsJob?.cancel()
        loadProsJob = viewModelScope.launch {
            _state.value = BookingUiState.Loading
            repository.getActiveProfiles(categoryId)
                .onSuccess { list ->
                    _state.value = BookingUiState.Success(
                        professionals = list, 
                        filteredProfessionals = list
                    )
                    filterProfessionals()
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    if (e is com.pointcheck.core.network.ApiException && e.code in listOf(401, 403)) return@onFailure
                    _state.value = BookingUiState.Error(e.message ?: "Error al cargar profesionales")
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        updateState { it.copy(searchQuery = query) }
        filterProfessionals()
    }

    private fun filterProfessionals() {
        val current = _state.value as? BookingUiState.Success ?: return
        val query = current.searchQuery.trim().lowercase()
        val all = current.professionals
        
        val filtered = if (query.isEmpty()) {
            all
        } else {
            all.filter { 
                it.name.lowercase().contains(query) || 
                (it.specialty?.lowercase()?.contains(query) == true)
            }
        }
        updateState { it.copy(filteredProfessionals = filtered) }
    }

    fun selectProfessional(professional: SpecialistResponseDto) {
        updateState { it.copy(
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
            _state.value = BookingUiState.Loading
            
            repository.getActiveProfiles(categoryId) 
                .onSuccess { list ->
                    val found = list.find { 
                        it.id.trim().equals(id.trim(), ignoreCase = true)
                    }
                    
                    val newState = BookingUiState.Success(
                        professionals = list,
                        filteredProfessionals = list,
                        selectedProfessional = found
                    ).let { it.copy(isValid = validate(it)) }
                    
                    _state.value = newState
                    
                    if (found != null) {
                        loadServicesForProfessional(found.id)
                        loadWeather(found.city)
                        loadAvailabilityIfPossible()
                    }
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    if (e is com.pointcheck.core.network.ApiException && e.code in listOf(401, 403)) return@onFailure
                    _state.value = BookingUiState.Error(e.message ?: "Error al cargar profesional")
                }
        }
    }

    private fun loadWeather(city: String?) {
        if (city == null) return
        viewModelScope.launch {
            repository.getWeather(city)
                .onSuccess { w -> updateState { it.copy(weather = w) } }
        }
    }

    private fun loadServicesForProfessional(professionalProfileId: String) {
        viewModelScope.launch {
            repository.getServices(professionalProfileId)
                .onSuccess { list ->
                    updateState { it.copy(services = list) }
                }
        }
    }

    fun selectService(service: ServiceResponseDto) {
        updateState { s -> s.copy(selectedService = service).let { it.copy(isValid = validate(it)) } }
        loadAvailabilityIfPossible()
    }

    fun setReservationDateTime(millis: Long) {
        // 1. Extraer los valores nominales (Año, Mes, Día) tratando el tiempo como UTC Puro
        val utcCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        utcCal.timeInMillis = millis
        val year = utcCal.get(Calendar.YEAR)
        val month = utcCal.get(Calendar.MONTH)
        val day = utcCal.get(Calendar.DAY_OF_MONTH)

        // 2. Reconstruir la fecha en el calendario LOCAL del dispositivo
        val localCal = Calendar.getInstance()
        localCal.set(year, month, day, 9, 0, 0) // Forzamos las 09:00 AM del día exacto seleccionado
        localCal.set(Calendar.MILLISECOND, 0)
        
        updateState { s -> 
            s.copy(reservationStartMillis = localCal.timeInMillis, selectedSlot = null)
                .let { it.copy(isValid = validate(it)) } 
        }
        loadAvailabilityIfPossible()
    }

    private fun loadAvailabilityIfPossible() {
        val s = _state.value as? BookingUiState.Success ?: return
        if (s.selectedProfessional != null && s.reservationStartMillis != null) {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(s.reservationStartMillis)

            availabilityJob?.cancel()
            availabilityJob = viewModelScope.launch {
                updateState { it.copy(isAvailabilityLoading = true) }
                repository.getAvailability(s.selectedProfessional.id, dateStr)
                    .onSuccess { resp ->
                        updateState { it.copy(availableSlots = resp.availableSlots, isAvailabilityLoading = false) }
                    }
                    .onFailure { e ->
                        if (e is CancellationException) throw e
                        updateState { it.copy(isAvailabilityLoading = false, availableSlots = emptyList()) }
                        _state.value = BookingUiState.Error(e.message ?: "Error de disponibilidad")
                    }
            }
        }
    }

    fun updateReservationTimeFromSlot(slot: String) {
        val s = _state.value as? BookingUiState.Success ?: return
        val currentMillis = s.reservationStartMillis ?: return
        
        val cal = Calendar.getInstance()
        cal.timeInMillis = currentMillis
        val parts = slot.split(":")
        if (parts.size == 2) {
            cal.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
            cal.set(Calendar.MINUTE, parts[1].toInt())
        }
        
        updateState { it.copy(
            reservationStartMillis = cal.timeInMillis,
            selectedSlot = slot
        ).let { it.copy(isValid = validate(it)) } }
    }

    fun setNotes(notes: String) {
        updateState { it.copy(notes = notes) }
    }

    fun setPaymentMethod(method: String) {
        updateState { s -> s.copy(paymentMethod = method).let { it.copy(isValid = validate(it)) } }
    }

    private fun validate(s: BookingUiState.Success): Boolean {
        val isDayUnit = s.selectedService?.priceUnit == "DAY"
        val isAtHome = s.selectedService?.isAtHome == true
        
        val basicValid = s.selectedProfessional != null &&
               s.selectedService != null &&
               s.reservationStartMillis != null &&
               s.paymentMethod != null &&
               (s.selectedSlot != null || isDayUnit)
               
        return if (isAtHome) {
            val hasAddress = !userAddress.isNullOrBlank()
            updateState { it.copy(isAtHomeAddressMissing = !hasAddress) }
            basicValid && (hasAddress || s.notes.trim().length >= 5)
        } else {
            updateState { it.copy(isAtHomeAddressMissing = false) }
            basicValid
        }
    }

    fun loadMyReservations(userId: String) {
        viewModelScope.launch {
            _state.value = BookingUiState.Loading
            repository.getReservationsByClient(userId)
                .onSuccess { list ->
                    _reservations.value = list
                    _state.value = BookingUiState.Success()
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    if (e is com.pointcheck.core.network.ApiException && e.code in listOf(401, 403)) return@onFailure
                    _reservations.value = emptyList()
                    _state.value = BookingUiState.Error(e.message ?: "Error al cargar reservas")
                }
        }
    }

    fun createReservation(onDone: () -> Unit) {
        val s = _state.value as? BookingUiState.Success ?: return
        if (!validate(s)) return

        val specialistProfileId = s.selectedProfessional?.id
        val serviceId = s.selectedService?.id

        if (specialistProfileId == null || serviceId == null) {
            _state.value = BookingUiState.Error("Información de selección incompleta")
            return
        }

        viewModelScope.launch {
            _state.value = BookingUiState.Loading
            val userId = prefs.userId.first() ?: return@launch
            
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val startStr = sdf.format(s.reservationStartMillis)
            
            val request = ReservationRequestDto(
                clientId = userId,
                specialistProfileId = specialistProfileId,
                serviceId = serviceId,
                reservationStart = startStr,
                notes = s.notes,
                paymentMethod = s.paymentMethod
            )

            repository.createReservation(request)
                .onSuccess {
                    _state.value = BookingUiState.Success(successMessage = "Reserva creada con éxito")
                    
                    s.reservationStartMillis?.let { startMillis ->
                        val reminderTime = startMillis - (60 * 60 * 1000)
                        if (reminderTime > System.currentTimeMillis()) {
                            scheduler.scheduleAt(
                                reminderTime,
                                "Recordatorio de Cita",
                                "Tu cita para ${s.selectedService?.name} comienza en 1 hora."
                            )
                        }
                    }
                    onDone()
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    if (e is com.pointcheck.core.network.ApiException && e.code in listOf(401, 403)) return@onFailure
                    _state.value = BookingUiState.Error(e.message ?: "Error al crear reserva")
                }
        }
    }

    fun cancelReservation(reservationId: String) {
        viewModelScope.launch {
            val userId = prefs.userId.first() ?: return@launch
            _state.value = BookingUiState.Loading
            repository.cancelReservation(userId, reservationId)
                .onSuccess {
                    loadDualAgenda(userId)
                    _state.value = BookingUiState.Success(successMessage = "Reserva cancelada")
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    _state.value = BookingUiState.Error(e.message ?: "Error al cancelar")
                }
        }
    }

    fun clearSuccess() {
        val current = _state.value
        if (current is BookingUiState.Success) {
            _state.value = current.copy(successMessage = null)
        }
    }
}
