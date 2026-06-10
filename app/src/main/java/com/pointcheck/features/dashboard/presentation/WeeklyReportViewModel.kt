package com.pointcheck.features.dashboard.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pointcheck.core.network.ApiClient
import com.pointcheck.core.prefs.UserPreferences
import com.pointcheck.features.dashboard.data.dto.MonthlyReportResponseDto
import com.pointcheck.features.dashboard.data.dto.WeeklyReportResponseDto
import com.pointcheck.features.dashboard.data.dto.ReportSummaryResponseDto
import com.pointcheck.features.dashboard.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.pointcheck.features.services.data.dto.ServiceResponseDto

enum class ReportPeriod { WEEKLY, MONTHLY }

/**
 * Representa el estado de la interfaz de usuario para los reportes de rendimiento y BI.
 *
 * @property report Datos del reporte semanal.
 * @property monthlyReport Datos del reporte mensual.
 * @property summary Resumen general de métricas.
 * @property services Lista de servicios del profesional para filtrado.
 * @property selectedServiceId ID del servicio seleccionado para filtrar el reporte.
 * @property isLoading Indica si el reporte se está generando o descargando.
 * @property error Mensaje de error a mostrar.
 * @property weekOffset Desplazamiento de semanas hacia atrás desde la fecha actual.
 * @property monthOffset Desplazamiento de meses hacia atrás.
 * @property period Tipo de reporte visualizado (Semanal o Mensual).
 * @property exportContent Contenido en formato CSV listo para ser guardado o compartido.
 */
data class WeeklyReportUiState(
    val report: WeeklyReportResponseDto? = null,
    val monthlyReport: MonthlyReportResponseDto? = null,
    val summary: ReportSummaryResponseDto? = null,
    val services: List<ServiceResponseDto> = emptyList(),
    val selectedServiceId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val weekOffset: Int = 0,
    val monthOffset: Int = 0,
    val period: ReportPeriod = ReportPeriod.WEEKLY,
    val exportContent: String? = null
)

/**
 * ViewModel especializado en la analítica de negocio para el profesional.
 * Gestiona reportes temporales, filtrado por servicios y la exportación de datos financieros.
 *
 * @param application Contexto de la aplicación.
 */
class WeeklyReportViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DashboardRepository(ApiClient.instance)
    private val prefs = UserPreferences(application)

    private val _state = MutableStateFlow(WeeklyReportUiState())
    val state: StateFlow<WeeklyReportUiState> = _state

    init {
        loadServicesAndReport()
    }

    private fun loadServicesAndReport() {
        viewModelScope.launch {
            val userId = prefs.userId.first() ?: return@launch
            
            // Cargar servicios del especialista para los filtros de búsqueda
            ApiClient.instance.getProfessionalProfileByUserId(userId).body()?.let { profile ->
                ApiClient.instance.getServicesByProfessionalProfileId(profile.id).body()?.let { list ->
                    _state.update { it.copy(services = list) }
                }
            }
            
            loadReport()
        }
    }

    /**
     * Carga el reporte correspondiente basándose en el periodo (Semanal/Mensual) y filtros actuales.
     * Soporta navegación temporal mediante offsets.
     */
    fun loadReport() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val userId = prefs.userId.first()
            if (userId != null) {
                val currentState = _state.value
                val result = if (currentState.period == ReportPeriod.WEEKLY) {
                    repository.getWeeklyReport(userId, currentState.weekOffset, currentState.selectedServiceId)
                } else {
                    repository.getMonthlyReport(userId, currentState.monthOffset, currentState.selectedServiceId)
                }

                result.fold(
                    onSuccess = { data ->
                        when (data) {
                            is WeeklyReportResponseDto -> _state.update { it.copy(report = data, isLoading = false) }
                            is MonthlyReportResponseDto -> _state.update { it.copy(monthlyReport = data, isLoading = false) }
                            is ReportSummaryResponseDto -> _state.update { it.copy(summary = data, isLoading = false) }
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

    /**
     * Aplica un filtro por servicio específico al reporte.
     * @param serviceId ID del servicio o null para ver todos.
     */
    fun setServiceFilter(serviceId: String?) {
        _state.update { it.copy(selectedServiceId = serviceId) }
        loadReport()
    }

    /**
     * Cambia la escala temporal del reporte.
     * @param period Periodo deseado (WEEKLY o MONTHLY).
     */
    fun setPeriod(period: ReportPeriod) {
        _state.update { it.copy(period = period) }
        loadReport()
    }

    /**
     * Navega en el tiempo (atrás o adelante) según el periodo actual.
     * @param delta Cantidad de periodos a desplazar (ej. -1 para la semana anterior).
     */
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

    /**
     * Solicita al backend la versión exportable (CSV) del reporte visualizado.
     * El contenido resultante se guarda en [WeeklyReportUiState.exportContent].
     */
    fun exportReport() {
        viewModelScope.launch {
            val userId = prefs.userId.first() ?: return@launch
            val currentState = _state.value
            val result = if (currentState.period == ReportPeriod.WEEKLY) {
                repository.exportWeeklyReport(userId, currentState.weekOffset, currentState.selectedServiceId)
            } else {
                repository.exportMonthlyReport(userId, currentState.monthOffset, currentState.selectedServiceId)
            }
            
            result.fold(
                onSuccess = { csv ->
                    if (csv.isNullOrBlank()) {
                        _state.update { it.copy(error = "No hay datos para exportar en este periodo") }
                    } else {
                        _state.update { it.copy(exportContent = csv) }
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(error = "Error al exportar: ${e.message}") }
                }
            )
        }
    }

    /** Limpia el contenido exportado del estado. */
    fun clearExport() {
        _state.update { it.copy(exportContent = null) }
    }

    /** Limpia el mensaje de error del estado. */
    fun clearError() = _state.update { it.copy(error = null) }
}
