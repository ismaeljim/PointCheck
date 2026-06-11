package com.pointcheck.features.dashboard.presentation

import android.app.Application
import android.util.Log
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
import kotlinx.coroutines.Job

/**
 * Representa el estado de la interfaz de usuario para el panel de control principal (Dashboard).
 * El contenido de este estado varía significativamente según el rol del usuario (Cliente, Profesional, Admin).
 *
 * @property metrics Métricas de alto nivel (total usuarios, citas, etc.).
 * @property clientDashboard Datos específicos para el rol de cliente (próximas citas, notificaciones).
 * @property reportSummary Resumen de actividad y desempeño para el rol de profesional.
 * @property weather Información climática basada en la ubicación de la próxima cita.
 * @property isLoading Indica si los datos principales están en proceso de carga.
 * @property isLoadingWeather Indica si la información del clima se está recuperando.
 * @property error Mensaje de error a mostrar en caso de fallos en la red o el servidor.
 * @property userName Nombre del usuario autenticado para personalización de la UI.
 * @property userRole Rol del usuario para determinar qué componentes mostrar.
 * @property categories Lista de categorías de servicios disponibles.
 * @property adminUsers Lista de usuarios para gestión administrativa (solo ADMIN).
 * @property financialReport Datos de facturación y ganancias globales (solo ADMIN).
 * @property adminSettings Configuraciones globales del sistema (solo ADMIN).
 * @property auditLogs Registro de eventos críticos del sistema (solo ADMIN).
 */
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
    val categories: List<CategoryDto> = emptyList(),
    val adminUsers: List<com.pointcheck.features.auth.data.dto.UserResponseDto> = emptyList(),
    val financialReport: Map<String, Any>? = null,
    val adminSettings: List<com.pointcheck.features.dashboard.data.dto.GlobalSettingDto> = emptyList(),
    val auditLogs: List<com.pointcheck.features.admin.data.dto.AuditLogDto> = emptyList()
)

/**
 * ViewModel central para la gestión del panel de control.
 * Implementa una carga de datos polimórfica basada en el rol del usuario.
 * Orquestra la recuperación de métricas, reportes, clima y notificaciones en una única fuente de verdad.
 *
 * @param application Contexto de la aplicación.
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DashboardRepository(ApiClient.instance)
    private val categoryApi = ApiClient.retrofitInstance.create(CategoryApi::class.java)
    private val prefs = UserPreferences(application)

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state

    private var loadJob: Job? = null

    init {
        Log.d("DashboardVM", "Iniciando DashboardViewModel")
        loadDashboard()
    }

    /**
     * Coordina la carga de datos del Dashboard.
     * Determina el rol del usuario desde las preferencias y dispara las peticiones correspondientes
     * (Admin, Profesional o Cliente).
     */
    fun loadDashboard() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            Log.d("DashboardVM", "loadDashboard() ejecutado")
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                val userId = prefs.userId.first()
                val role = prefs.role.first() ?: "CLIENT"
                val name = prefs.name.first() ?: "Usuario"
                
                Log.d("DashboardVM", "User data: id=$userId, role=$role, name=$name")

                _state.update { it.copy(userName = name, userRole = role) }

                if (userId != null) {
                    loadCategories()

                    if (role == "ADMIN") {
                        Log.d("DashboardVM", "Cargando datos ADMIN")
                        repository.getDashboardMetrics(userId, role)
                            .onSuccess { metrics ->
                                _state.update { it.copy(metrics = metrics) }
                            }
                        loadAdminData()
                    } else if (role == "SPECIALIST" || role == "PROFESSIONAL") {
                        Log.d("DashboardVM", "Cargando datos SPECIALIST")
                        
                        // 1. Cargar métricas generales para verificar estado del perfil
                        repository.getDashboardMetrics(userId, role)
                            .onSuccess { metrics ->
                                if (!metrics.isProfileComplete) {
                                    _state.update { it.copy(error = "PROFILE_INCOMPLETE", isLoading = false) }
                                } else {
                                    _state.update { it.copy(metrics = metrics) }
                                    // Guardar especialidad si cambió
                                    metrics.specialty?.let { sp ->
                                        viewModelScope.launch { prefs.saveSpecialty(sp) }
                                    }
                                }
                            }

                        // 2. Cargar resumen de reportes
                        repository.getReportSummaryBySpecialist(userId)
                            .onSuccess { summary ->
                                Log.d("DashboardVM", "Reporte cargado con éxito")
                                _state.update { it.copy(reportSummary = summary, isLoading = false) }
                            }
                            .onFailure { e ->
                                Log.e("DashboardVM", "Error en reporte: ${e.message}")
                                _state.update { it.copy(error = "Error al cargar reporte: ${e.message}", isLoading = false) }
                            }
                    } else {
                        Log.d("DashboardVM", "Cargando datos CLIENT")
                        repository.getClientDashboard(userId)
                            .onSuccess { dashboard ->
                                Log.d("DashboardVM", "Dashboard cliente cargado")
                                _state.update { it.copy(clientDashboard = dashboard, isLoading = false) }
                                // Cargar clima si hay una cita próxima con ciudad
                                dashboard.nextAppointment?.city?.let { city ->
                                    loadWeather(city)
                                }
                            }
                            .onFailure { e ->
                                Log.e("DashboardVM", "Error en dashboard cliente: ${e.message}")
                                _state.update { it.copy(error = "Error al cargar dashboard: ${e.message}", isLoading = false) }
                            }
                    }
                } else {
                    Log.w("DashboardVM", "No hay userId en preferencias")
                    _state.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                Log.e("DashboardVM", "Excepción fatal en loadDashboard: ${e.message}", e)
                _state.update { it.copy(isLoading = false, error = "Error inesperado: ${e.message}") }
            }
        }
    }

    /**
     * Carga datos extendidos para usuarios con rol de Administrador.
     */
    private fun loadAdminData() {
        viewModelScope.launch {
            // Cargar usuarios para auditoría
            repository.getAllUsers()
                .onSuccess { users ->
                    _state.update { it.copy(adminUsers = users) }
                }
            
            // Cargar configuraciones
            repository.getSettings()
                .onSuccess { settings ->
                    _state.update { it.copy(adminSettings = settings) }
                }
            
            // Cargar reporte financiero global
            repository.getFinancialReport()
                .onSuccess { report ->
                    _state.update { it.copy(financialReport = report) }
                }

            // Cargar Logs de Auditoría
            repository.getAuditLogs()
                .onSuccess { logs ->
                    _state.update { it.copy(auditLogs = logs, isLoading = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = "Error Admin: ${e.message}", isLoading = false) }
                }
        }
    }

    /**
     * Actualiza una configuración global del sistema (Solo Admin).
     *
     * @param key Clave de la configuración.
     * @param value Nuevo valor.
     */
    fun updateSetting(key: String, value: String) {
        viewModelScope.launch {
            repository.updateSetting(key, value)
                .onSuccess { loadAdminData() }
        }
    }

    /**
     * Cambia el estado de activación de un usuario (Solo Admin).
     *
     * @param userId ID del usuario.
     */
    fun toggleUserStatus(userId: String) {
        viewModelScope.launch {
            repository.toggleUserStatus(userId)
                .onSuccess { loadAdminData() } // Recargar lista
        }
    }

    /**
     * Obtiene el catálogo de categorías para visualización en el Dashboard.
     */
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

    /**
     * Consulta el clima actual para una ciudad.
     *
     * @param city Nombre de la ciudad.
     */
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

    /**
     * Marca una notificación como leída tanto en el servidor como en el estado local.
     *
     * @param notificationId ID de la notificación.
     */
    fun markAsRead(notificationId: String) {
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

    /** Limpia el mensaje de error del estado. */
    fun clearError() = _state.update { it.copy(error = null) }
}
