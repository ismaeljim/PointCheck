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
import com.pointcheck.features.auth.data.dto.UserResponseDto
import com.pointcheck.features.dashboard.data.dto.GlobalSettingDto
import com.pointcheck.features.admin.data.dto.AuditLogDto
import com.pointcheck.core.network.ApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

enum class AdminChartType { LINE, BAR }

/**
 * Jerarquía de estados para el Dashboard.
 * Implementa el patrón de "Single Source of Truth" con estados atómicos.
 */
sealed class DashboardUiState {
    object Loading : DashboardUiState()
    
    data class Success(
        val userName: String,
        val userRole: String,
        val metrics: DashboardMetricsDto = DashboardMetricsDto(),
        val clientDashboard: ClientDashboardResponseDto? = null,
        val reportSummary: ReportSummaryResponseDto? = null,
        val weather: WeatherResponseDto? = null,
        val categories: List<CategoryDto> = emptyList(),
        val adminUsers: List<UserResponseDto> = emptyList(),
        val financialReport: Map<String, Any>? = null,
        val adminSettings: List<GlobalSettingDto> = emptyList(),
        val auditLogs: List<AuditLogDto> = emptyList(),
        val isLoadingWeather: Boolean = false,
        val adminChartType: AdminChartType = AdminChartType.LINE
    ) : DashboardUiState()
    
    data class Error(val message: String) : DashboardUiState()
    
    object ProfileIncomplete : DashboardUiState()
}

/**
 * ViewModel encargado de la lógica de negocio del Dashboard.
 * 
 * Gestiona la carga de datos multidimensionales (Métricas, Reservas, Clima, Categorías)
 * basándose en el rol del usuario autenticado (ADMIN, SPECIALIST, CLIENT).
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DashboardRepository(ApiClient.instance)
    private val categoryApi = ApiClient.retrofitInstance.create(CategoryApi::class.java)
    private val prefs = UserPreferences(application)

    private val _state = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val state: StateFlow<DashboardUiState> = _state

    private var loadJob: Job? = null

    init {
        loadDashboard()
    }

    /**
     * Carga o refresca todos los datos necesarios para el Dashboard según el rol del usuario.
     * @param silent Si es true, no cambia el estado a Loading si ya hay datos cargados.
     */
    fun loadDashboard(silent: Boolean = false) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (!silent || _state.value !is DashboardUiState.Success) {
                _state.value = DashboardUiState.Loading
            }

            try {
                val userId = prefs.userId.first()
                val role = prefs.role.first() ?: "CLIENT"
                val name = prefs.name.first() ?: "Usuario"

                if (userId == null) {
                    _state.value = DashboardUiState.Error("Sesión no válida")
                    return@launch
                }

                // Carga de categorías común
                val categories = try { 
                    categoryApi.getCategories() 
                } catch (e: Exception) { 
                    emptyList() 
                }

                when (role) {
                    "ADMIN" -> {
                        val metricsResult = repository.getAdminMetrics()
                        val usersResult = repository.getAllUsers()
                        val settingsResult = repository.getSettings()
                        val logsResult = repository.getAuditLogs()
                        val financeResult = repository.getFinancialReport()

                        // SPRINT 4 FIX: No abortar silenciosamente. Si hay error de seguridad, el Interceptor
                        // ya maneja el logout. Aquí debemos informar el error para salir de 'Loading'.
                        val results = listOf(metricsResult, usersResult, settingsResult, logsResult, financeResult)
                        val firstError = results.mapNotNull { it.exceptionOrNull() }.firstOrNull()
                        if (firstError != null) {
                            _state.value = DashboardUiState.Error(firstError.localizedMessage ?: "Error de permisos")
                            return@launch
                        }

                        _state.value = DashboardUiState.Success(
                            userName = name,
                            userRole = role,
                            metrics = metricsResult.getOrNull() ?: DashboardMetricsDto(),
                            adminUsers = usersResult.getOrNull() ?: emptyList(),
                            adminSettings = settingsResult.getOrNull() ?: emptyList(),
                            auditLogs = logsResult.getOrNull() ?: emptyList(),
                            financialReport = financeResult.getOrNull(),
                            categories = categories
                        )
                    }
                    "SPECIALIST", "PROFESSIONAL" -> {
                        var profileId = prefs.professionalProfileId.first() ?: ""
                        
                        if (profileId.isEmpty()) {
                            val profileResult = repository.getProfessionalProfileByUserId(userId)
                            if (profileResult.exceptionOrNull() is ApiException && (profileResult.exceptionOrNull() as ApiException).code in listOf(401, 403)) return@launch
                            profileId = profileResult.getOrNull()?.id ?: ""
                            if (profileId.isNotEmpty()) {
                                prefs.saveProfessionalProfileId(profileId)
                            }
                        }

                        val metricsResult = repository.getDashboardMetrics()
                        
                        val metrics = metricsResult.getOrNull()

                        if (metricsResult.exceptionOrNull() is ApiException && (metricsResult.exceptionOrNull() as ApiException).code == 401) return@launch
                        
                        if (metrics == null && metricsResult.isFailure) {
                            _state.value = DashboardUiState.Error(metricsResult.exceptionOrNull()?.localizedMessage ?: "Error al cargar métricas")
                            return@launch
                        }

                        if (metrics?.isProfileComplete == false) {
                            _state.value = DashboardUiState.ProfileIncomplete
                        } else {
                            val summaryResult = if (profileId.isNotEmpty()) {
                                repository.getReportSummaryBySpecialist(profileId)
                            } else {
                                Result.success(ReportSummaryResponseDto())
                            }
                            
                            if (summaryResult.exceptionOrNull() is ApiException && (summaryResult.exceptionOrNull() as ApiException).code in listOf(401, 403)) return@launch

                            _state.value = DashboardUiState.Success(
                                userName = name,
                                userRole = role,
                                metrics = metrics ?: DashboardMetricsDto(),
                                reportSummary = summaryResult.getOrNull() ?: ReportSummaryResponseDto(),
                                categories = categories
                            )
                        }
                    }
                    else -> { // CLIENT
                        val dashboardResult = repository.getClientDashboard(userId)
                        if (dashboardResult.exceptionOrNull() is ApiException && (dashboardResult.exceptionOrNull() as ApiException).code in listOf(401, 403)) return@launch
                        
                        val dashboard = dashboardResult.getOrNull()
                        _state.value = DashboardUiState.Success(
                            userName = name,
                            userRole = role,
                            clientDashboard = dashboard,
                            categories = categories
                        )
                        dashboard?.nextAppointment?.city?.let { loadWeather(it) }
                    }
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _state.value = DashboardUiState.Error(e.localizedMessage ?: "Error desconocido")
            }
        }
    }

    private fun loadWeather(city: String) {
        val currentState = _state.value
        if (currentState is DashboardUiState.Success) {
            viewModelScope.launch {
                repository.getWeather(city).onSuccess { w ->
                    _state.value = currentState.copy(weather = w)
                }
            }
        }
    }

    fun markAsRead(notificationId: String) {
        val currentState = _state.value
        if (currentState is DashboardUiState.Success && currentState.clientDashboard != null) {
            viewModelScope.launch {
                repository.markNotificationAsRead(notificationId).onSuccess {
                    val updatedNotifications = currentState.clientDashboard.recentNotifications?.map {
                        if (it.id == notificationId) it.copy(isRead = true) else it
                    } ?: emptyList()
                    _state.value = currentState.copy(
                        clientDashboard = currentState.clientDashboard.copy(recentNotifications = updatedNotifications)
                    )
                }.onFailure { e ->
                    if (e is ApiException && (e.code == 401 || e.code == 403)) return@onFailure
                }
            }
        }
    }

    // --- Admin Actions ---

    fun toggleUserStatus(userId: String) {
        viewModelScope.launch {
            repository.toggleUserStatus(userId).onSuccess {
                loadDashboard(silent = true)
            }.onFailure { e ->
                if (e is ApiException && (e.code == 401 || e.code == 403)) return@onFailure
                // No cambiamos el estado global a Error para no bloquear la UI
            }
        }
    }

    fun updateSetting(key: String, value: String) {
        viewModelScope.launch {
            repository.updateSetting(key, value).onSuccess {
                loadDashboard(silent = true)
            }.onFailure { e ->
                if (e is ApiException && (e.code == 401 || e.code == 403)) return@onFailure
                // Manejo silencioso de error
            }
        }
    }

    fun loadAdminData() {
        loadDashboard()
    }

    fun toggleAdminChartType(type: AdminChartType) {
        val currentState = _state.value
        if (currentState is DashboardUiState.Success) {
            _state.value = currentState.copy(adminChartType = type)
        }
    }
}
