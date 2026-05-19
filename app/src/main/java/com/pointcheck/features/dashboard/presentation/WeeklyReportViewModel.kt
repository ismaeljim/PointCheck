package com.pointcheck.features.dashboard.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.features.dashboard.data.dto.MonthlyReportResponseDto
import com.pointcheck.features.dashboard.data.dto.WeeklyReportResponseDto
import com.pointcheck.features.dashboard.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ReportPeriod { WEEKLY, MONTHLY }

data class WeeklyReportUiState(
    val report: WeeklyReportResponseDto? = null,
    val monthlyReport: MonthlyReportResponseDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val weekOffset: Int = 0,
    val monthOffset: Int = 0,
    val period: ReportPeriod = ReportPeriod.WEEKLY,
    val exportContent: String? = null
)

class WeeklyReportViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DashboardRepository(ApiClient.instance)
    private val prefs = UserPreferences(application)

    private val _state = MutableStateFlow(WeeklyReportUiState())
    val state: StateFlow<WeeklyReportUiState> = _state

    init {
        loadReport()
    }

    fun loadReport() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val userId = prefs.userId.first()
            if (userId != null) {
                val currentState = _state.value
                val result = if (currentState.period == ReportPeriod.WEEKLY) {
                    repository.getWeeklyReport(userId, currentState.weekOffset)
                } else {
                    repository.getMonthlyReport(userId, currentState.monthOffset)
                }

                result.fold(
                    onSuccess = { data ->
                        if (data is WeeklyReportResponseDto) {
                            _state.update { it.copy(report = data, isLoading = false) }
                        } else if (data is MonthlyReportResponseDto) {
                            _state.update { it.copy(monthlyReport = data, isLoading = false) }
                        }
                    },
                    onFailure = { e ->
                        _state.update { it.copy(error = "Error: ${e.message}", isLoading = false) }
                    }
                )
            } else {
                _state.update { it.copy(error = "Usuario no identificado", isLoading = false) }
            }
        }
    }

    fun setPeriod(period: ReportPeriod) {
        _state.update { it.copy(period = period) }
        loadReport()
    }

    fun changeOffset(delta: Int) {
        _state.update { 
            if (it.period == ReportPeriod.WEEKLY) {
                it.copy(weekOffset = it.weekOffset + delta)
            } else {
                it.copy(monthOffset = it.monthOffset + delta)
            }
        }
        loadReport()
    }

    fun exportReport() {
        viewModelScope.launch {
            val userId = prefs.userId.first() ?: return@launch
            val currentState = _state.value
            val result = if (currentState.period == ReportPeriod.WEEKLY) {
                repository.exportWeeklyReport(userId, currentState.weekOffset)
            } else {
                repository.exportMonthlyReport(userId, currentState.monthOffset)
            }
            
            result.fold(
                onSuccess = { csv ->
                    _state.update { it.copy(exportContent = csv) }
                },
                onFailure = { e ->
                    _state.update { it.copy(error = "Error al exportar: ${e.message}") }
                }
            )
        }
    }

    fun clearExport() {
        _state.update { it.copy(exportContent = null) }
    }
}
