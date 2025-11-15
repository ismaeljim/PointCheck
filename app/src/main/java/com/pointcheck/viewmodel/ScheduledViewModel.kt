package com.pointcheck.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.model.Reservation
import com.pointcheck.repository.RoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScheduledViewModel(application: Application, private val reservationId: Int) : AndroidViewModel(application) {
    private val repository = RoomRepository(application)

    val reservation: Flow<Reservation?> = repository.getReservationById(reservationId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateReservationName(newName: String) {
        viewModelScope.launch {
            val res = reservation.first()
            res?.let {
                repository.updateReservation(it.copy(name = newName))
            }
        }
    }

    fun deleteReservation() {
        viewModelScope.launch {
            repository.deleteReservation(reservationId)
        }
    }
}
