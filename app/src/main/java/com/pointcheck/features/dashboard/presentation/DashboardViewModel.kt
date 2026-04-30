package com.pointcheck.features.dashboard.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.features.dashboard.data.dto.DashboardMetricsDto
import com.pointcheck.features.dashboard.data.dto.ReportSummaryResponseDto
import com.pointcheck.features.dashboard.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val metrics: DashboardMetricsDto = DashboardMetricsDto(),
    val reportSummary: ReportSummaryResponseDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val userName: String = "",
    val userRole: String = ""
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DashboardRepository(ApiClient.instance)
    private val prefs = UserPreferences(application)

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val userId = prefs.userId.first()
            val role = prefs.role.first() ?: "CLIENT"
            val name = prefs.name.first() ?: "Usuario"

            _state.update { it.copy(userName = name, userRole = role) }

            if (userId != null) {
                if (role == "SPECIALIST" || role == "PROFESSIONAL") {
                    repository.getReportSummaryBySpecialist(userId)
                        .onSuccess { summary ->
                            _state.update { it.copy(reportSummary = summary, isLoading = false) }
                        }
                        .onFailure { e ->
                            _state.update { it.copy(error = "Error al cargar reporte: ${e.message}", isLoading = false) }
                        }
                } else {
                    repository.getDashboardMetrics(userId, role)
                        .onSuccess { metrics ->
                            _state.update { it.copy(metrics = metrics, isLoading = false) }
                        }
                        .onFailure { e ->
                            _state.update { it.copy(error = "Error al cargar métricas: ${e.message}", isLoading = false) }
                        }
                }
            } else {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
