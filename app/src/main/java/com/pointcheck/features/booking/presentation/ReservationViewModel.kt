package com.pointcheck.features.booking.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.database.AppDatabase
import com.pointcheck.core.notifications.ReminderScheduler
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.features.booking.data.Reservation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class BookingUiState(
    val name: String = "",
    val epochMillis: Long? = null,
    val isValid: Boolean = false
)

class ReservationViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(BookingUiState())
    val state: StateFlow<BookingUiState> = _state
    private val scheduler = ReminderScheduler(application.applicationContext)
    private val db = AppDatabase.getDatabase(application)
    private val reservationDao = db.reservationDao()
    private val prefs = UserPreferences(application)

    @OptIn(ExperimentalCoroutinesApi::class)
    val reservations: Flow<List<Reservation>> = prefs.email.flatMapLatest { email ->
        email?.let { reservationDao.getUpcomingReservations(it, System.currentTimeMillis()) } ?: flowOf(emptyList())
    }

    fun setName(value: String) {
        val s = _state.value
        val n = s.copy(name = value)
        _state.value = n.copy(isValid = validate(n))
    }
    
    fun setEpoch(value: Long?) {
        val s = _state.value
        val n = s.copy(epochMillis = value)
        _state.value = n.copy(isValid = validate(n))
    }
    
    private fun validate(s: BookingUiState) =
        s.name.isNotBlank() && (s.epochMillis ?: 0L) > System.currentTimeMillis()

    fun confirmAndSchedule(onDone: () -> Unit) {
        val s = _state.value
        val epoch = s.epochMillis

        if (!s.isValid || epoch == null) return

        viewModelScope.launch {
            val userEmail = prefs.email.first() ?: ""

            if (userEmail.isNotEmpty()) {
                val reservation = Reservation(
                    userEmail = userEmail,
                    name = s.name,
                    epochMillis = epoch
                )
                reservationDao.insertReservation(reservation)
                scheduler.scheduleAt(epoch, "Recordatorio de reserva", "Hola ${s.name}, no olvides tu cita.")
                onDone()
            }
        }
    }

    fun deleteReservation(id: Int) {
        viewModelScope.launch {
            reservationDao.deleteReservation(id)
        }
    }
}
