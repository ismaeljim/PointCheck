package com.pointcheck.features.dashboard.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.features.dashboard.data.dto.ClientDashboardResponseDto
import com.pointcheck.features.dashboard.data.dto.DashboardMetricsDto
import com.pointcheck.features.dashboard.data.dto.ReportSummaryResponseDto
import com.pointcheck.features.dashboard.data.repository.DashboardRepository
import com.pointcheck.features.external.data.dto.WeatherResponseDto
import com.pointcheck.features.onboarding.presentation.dto.CategoryDto
import com.pointcheck.features.onboarding.presentation.CategoryApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val metrics: DashboardMetricsDto = DashboardMetricsDto(),
    val clientDashboard: ClientDashboardResponseDto? = null,
    val reportSummary: ReportSummaryResponseDto? = null,
    val weather: WeatherResponseDto? = null,
    val isLoading: Boolean = false,
    val isLoadingWeather: Boolean = false,
    val error: String? = null,
    val userName: String = "",
    val userRole: String = "",
    val categories: List<CategoryDto> = emptyList()
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DashboardRepository(ApiClient.instance)
    private val categoryApi = ApiClient.retrofitInstance.create(CategoryApi::class.java)
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
                if (role == "CLIENT") {
                    loadCategories()
                }
                if (role == "SPECIALIST" || role == "PROFESSIONAL") {
                    repository.getReportSummaryBySpecialist(userId)
                        .onSuccess { summary ->
                            _state.update { it.copy(reportSummary = summary, isLoading = false) }
                        }
                        .onFailure { e ->
                            _state.update { it.copy(error = "Error al cargar reporte: ${e.message}", isLoading = false) }
                        }
                } else {
                    repository.getClientDashboard(userId)
                        .onSuccess { dashboard ->
                            _state.update { it.copy(clientDashboard = dashboard, isLoading = false) }
                            // Cargar clima si hay una cita próxima con ciudad
                            dashboard.nextAppointment?.city?.let { city ->
                                loadWeather(city)
                            }
                        }
                        .onFailure { e ->
                            _state.update { it.copy(error = "Error al cargar dashboard: ${e.message}", isLoading = false) }
                        }
                }
            } else {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val cats = categoryApi.getCategories()
                _state.update { it.copy(categories = cats) }
            } catch (e: Exception) {
                // Silently fail or log, categories are secondary to core dashboard
            }
        }
    }

    private fun loadWeather(city: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingWeather = true) }
            repository.getWeather(city)
                .onSuccess { w ->
                    _state.update { it.copy(weather = w, isLoadingWeather = false) }
                }
                .onFailure {
                    _state.update { it.copy(isLoadingWeather = false) }
                }
        }
    }

    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(notificationId)
                .onSuccess {
                    // Actualizar estado local
                    _state.update { s ->
                        val updatedList = s.clientDashboard?.recentNotifications?.map {
                            if (it.id == notificationId) it.copy(isRead = true) else it
                        } ?: emptyList()
                        
                        s.copy(
                            clientDashboard = s.clientDashboard?.copy(recentNotifications = updatedList)
                        )
                    }
                }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}
