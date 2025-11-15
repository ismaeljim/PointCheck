package com.pointcheck.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ScheduledViewModelFactory(private val application: Application, private val reservationId: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduledViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScheduledViewModel(application, reservationId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
