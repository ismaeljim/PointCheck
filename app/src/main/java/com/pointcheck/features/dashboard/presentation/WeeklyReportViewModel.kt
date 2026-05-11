package com.pointcheck.features.dashboard.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.features.dashboard.data.dto.WeeklyReportResponseDto
import com.pointcheck.features.dashboard.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WeeklyReportUiState(
    val report: WeeklyReportResponseDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val weekOffset: Int = 0
)

class WeeklyReportViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DashboardRepository(ApiClient.instance)
    private val prefs = UserPreferences(application)

    private val _state = MutableStateFlow(WeeklyReportUiState())
    val state: StateFlow<WeeklyReportUiState> = _state

    init {
        loadReport()
    }

    fun loadReport(offset: Int = _state.value.weekOffset) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, weekOffset = offset) }
            
            val userId = prefs.userId.first()
            if (userId != null) {
                repository.getWeeklyReport(userId, offset)
                    .onSuccess { report ->
                        _state.update { it.copy(report = report, isLoading = false) }
                    }
                    .onFailure { e ->
                        _state.update { it.copy(error = "Error: ${e.message}", isLoading = false) }
                    }
            } else {
                _state.update { it.copy(error = "Usuario no identificado", isLoading = false) }
            }
        }
    }

    fun changeWeek(delta: Int) {
        val newOffset = _state.value.weekOffset + delta
        loadReport(newOffset)
    }
}
