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
    val searchQuery: String = "",
    val filteredProfessionals: List<SpecialistResponseDto> = emptyList(),
    val isLoading: Boolean = false,
    val isAvailabilityLoading: Boolean = false,
    val isAtHomeAddressMissing: Boolean = false,
    val isValid: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel encargado de orquestar el flujo de reserva de citas.
 * Gestiona la selección de profesional, servicio, fecha y hora, además de la confirmación de pagos
 * y la visualización de la agenda (reservas y atenciones).
 *
 * Implementa un flujo multipaso: Selección Especialista -> Servicio -> Fecha/Hora -> Confirmación.
 *
 * @param app Instancia de la aplicación para acceso a recursos y preferencias.
 */
class ReservationViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = ReservationRepository(ApiClient.instance)
    private val prefs = UserPreferences(app)

    private val _state = MutableStateFlow(BookingUiState())
    val state: StateFlow<BookingUiState> = _state

    private val _reservations = MutableStateFlow<List<ReservationResponseDto>>(emptyList())
    val reservations: StateFlow<List<ReservationResponseDto>> = _reservations

    private val _attentions = MutableStateFlow<List<ReservationResponseDto>>(emptyList())
    val attentions: StateFlow<List<ReservationResponseDto>> = _attentions

    init {
        loadProfessionals()
        observeUserAddress()
    }

    private var userAddress: String? = null

    private fun observeUserAddress() {
        viewModelScope.launch {
            prefs.address.collect { address ->
                userAddress = address
                _state.update { it.copy(isValid = validate(it)) }
            }
        }
    }

    /**
     * Carga tanto las reservas del cliente como las atenciones del especialista para un usuario.
     * Utilizado para alimentar la vista de agenda dual.
     *
     * @param userId Identificador del usuario.
     */
    fun loadDualAgenda(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // Cargar Mis Reservas (como cliente)
            val resResult = repository.getReservationsByClient(userId)
            // Cargar Mis Atenciones (como especialista)
            val attResult = repository.getReservationsBySpecialist(userId)

            resResult.onSuccess { list -> _reservations.value = list }
            attResult.onSuccess { list -> _attentions.value = list }

            _state.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Confirma el pago (ya sea efectivo o digital) y completa la reserva.
     * Utiliza el endpoint transaccional del backend para asegurar la facturación.
     */
    fun confirmPayment(reservationId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val userId = prefs.userId.first() ?: return@launch
            repository.confirmPayment(userId, reservationId)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, successMessage = "Cita completada y pago registrado con éxito") }
                    loadDualAgenda(userId)
                    loadMyReservations(userId)
                    onDone()
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = "Error al confirmar pago: ${e.message}") }
                }
        }
    }

    /** Limpia el error actual del estado. */

    /**
     * Carga la lista de profesionales activos del sistema, opcionalmente filtrados por categoría.
     *
     * @param categoryId ID de la categoría para filtrar (opcional).
     */
    fun loadProfessionals(categoryId: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.getActiveProfiles(categoryId)
                .onSuccess { list ->
                    _state.update { it.copy(
                        professionals = list, 
                        filteredProfessionals = list,
                        isLoading = false 
                    ) }
                    filterProfessionals()
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    /**
     * Actualiza el término de búsqueda y filtra la lista de profesionales.
     */
    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        filterProfessionals()
    }

    /**
     * Filtra los profesionales basándose en el nombre o especialidad.
     */
    private fun filterProfessionals() {
        val query = _state.value.searchQuery.trim().lowercase()
        val all = _state.value.professionals
        
        val filtered = if (query.isEmpty()) {
            all
        } else {
            all.filter { 
                it.name.lowercase().contains(query) || 
                (it.specialty?.lowercase()?.contains(query) == true)
            }
        }
        _state.update { it.copy(filteredProfessionals = filtered) }
    }

    /**
     * Selecciona un profesional para la reserva y carga sus servicios y el clima local.
     *
     * @param professional Objeto del especialista seleccionado.
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

    /**
     * Selecciona un profesional basado en su ID (o ID de usuario).
     * Si el profesional no está en la lista cargada, lo busca en el repositorio.
     *
     * @param id ID del profesional o del usuario.
     * @param categoryId Categoría de filtro si es necesario recargar la lista.
     */
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

    /**
     * Carga la información climática de una ciudad para informar al usuario sobre condiciones externas.
     */
    private fun loadWeather(city: String?) {
        if (city == null) return
        viewModelScope.launch {
            repository.getWeather(city)
                .onSuccess { w -> _state.update { it.copy(weather = w) } }
        }
    }

    /**
     * Carga el catálogo de servicios de un perfil profesional específico.
     */
    private fun loadServicesForProfessional(professionalProfileId: String) {
        viewModelScope.launch {
            repository.getServices(professionalProfileId)
                .onSuccess { list ->
                    _state.update { it.copy(services = list) }
                }
        }
    }

    /**
     * Selecciona un servicio del catálogo del profesional.
     */
    fun selectService(service: ServiceResponseDto) {
        _state.update { s -> s.copy(selectedService = service).let { it.copy(isValid = validate(it)) } }
        loadAvailabilityIfPossible()
    }

    /**
     * Establece la fecha de la reserva y gatilla la consulta de disponibilidad horaria.
     *
     * @param millis Fecha seleccionada en milisegundos.
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

    /**
     * Consulta al backend los horarios disponibles para el profesional y fecha seleccionados.
     */
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

    /**
     * Actualiza la hora de inicio de la reserva basándose en el slot seleccionado (HH:mm).
     *
     * @param slot Horario seleccionado en formato string.
     */
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

    /**
     * Establece notas adicionales para la reserva (como dirección o requerimientos).
     */
    fun setNotes(notes: String) {
        _state.update { it.copy(notes = notes) }
    }

    /**
     * Establece el método de pago seleccionado por el usuario.
     */
    fun setPaymentMethod(method: String) {
        _state.update { s -> s.copy(paymentMethod = method).let { it.copy(isValid = validate(it)) } }
    }

    /**
     * Valida que el formulario de reserva esté completo según las reglas de negocio.
     */
    private fun validate(s: BookingUiState): Boolean {
        val isDayUnit = s.selectedService?.priceUnit == "DAY"
        val isAtHome = s.selectedService?.isAtHome == true
        
        val basicValid = s.selectedProfessional != null &&
               s.selectedService != null &&
               s.reservationStartMillis != null &&
               s.paymentMethod != null &&
               (s.selectedSlot != null || isDayUnit)
               
        return if (isAtHome) {
            val hasAddress = !userAddress.isNullOrBlank()
            _state.update { it.copy(isAtHomeAddressMissing = !hasAddress) }
            // Para servicios a domicilio, debe tener dirección en perfil O en notas
            basicValid && (hasAddress || s.notes.trim().length >= 5)
        } else {
            _state.update { it.copy(isAtHomeAddressMissing = false) }
            basicValid
        }
    }

    /**
     * Carga las reservas históricas y futuras de un cliente.
     */
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

    /**
     * Ejecuta la petición para crear una nueva reserva en el sistema.
     *
     * @param onDone Callback ejecutado tras el éxito de la creación.
     */
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

    /**
     * Cancela una reserva existente.
     */
    fun cancelReservation(reservationId: String) {
        viewModelScope.launch {
            val userId = prefs.userId.first() ?: return@launch
            repository.cancelReservation(userId, reservationId)
                .onSuccess {
                    loadMyReservations(userId)
                    _state.update { it.copy(successMessage = "Reserva cancelada") }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message) }
                }
        }
    }

    /** Limpia el error actual del estado. */
    fun clearError() = _state.update { it.copy(error = null) }
    
    /** Limpia el mensaje de éxito del estado. */
    fun clearSuccess() = _state.update { it.copy(successMessage = null) }
}
