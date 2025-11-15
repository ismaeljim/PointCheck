package com.pointcheck.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.data.prefs.UserPreferences
import com.pointcheck.model.Reservation
import com.pointcheck.notifications.ReminderScheduler
import com.pointcheck.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class BookingUiState(
    val name: String = "",
    val epochMillis: Long? = null,
    val isValid: Boolean = false
)

class ReservationViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(BookingUiState())
    val state: StateFlow<BookingUiState> = _state
    private val scheduler = ReminderScheduler(application.applicationContext)
    private val repository = RoomRepository(application)
    private val prefs = UserPreferences(application)

    fun setName(value: String) {
        val s = _state.value
        val n = s.copy(name = value); _state.value = n.copy(isValid = validate(n))
    }
    fun setEpoch(value: Long?) {
        val s = _state.value
        val n = s.copy(epochMillis = value); _state.value = n.copy(isValid = validate(n))
    }
    private fun validate(s: BookingUiState) =
        s.name.isNotBlank() && (s.epochMillis ?: 0L) > System.currentTimeMillis()

    fun confirmAndSchedule(onDone: () -> Unit) {
        val s = _state.value
        // Usamos una variable local para evitar el !! y el acceso a un posible nulo
        val epoch = s.epochMillis

        if (!s.isValid || epoch == null) return

        viewModelScope.launch {
            val userEmail = prefs.email.first() ?: ""

            if (userEmail.isNotEmpty()) {
                val reservation = Reservation(
                    userEmail = userEmail,
                    name = s.name,
                    epochMillis = epoch // Usamos la variable local segura
                )
                repository.insertReservation(reservation)
                scheduler.scheduleAt(epoch, "Recordatorio de reserva", "Hola ${s.name}, no olvides tu cita.")
                onDone()
            }
        }
    }

    fun deleteReservation(id: Int) {
        viewModelScope.launch {
            repository.deleteReservation(id.toLong())
        }
    }
}
