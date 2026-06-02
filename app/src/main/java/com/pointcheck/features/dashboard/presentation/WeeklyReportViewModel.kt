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
 * AUDITORÍA TÉCNICA: Inteligencia de Negocio en Dispositivos Móviles
 * 
 * Este ViewModel gestiona la visualización de métricas de desempeño y la exportación de datos.
 * 
 * Hallazgos:
 * 1. [OK] Polimorfismo de Reportes: Maneja estados tanto semanales como mensuales de forma unificada.
 * 2. [OK] Estrategia de Filtros: Permite filtrar por tipo de servicio, lo que ayuda al especialista 
 *    a identificar qué prestaciones son más rentables.
 * 3. [OK] Navegación Temporal: Implementa 'offsets' para que el usuario pueda navegar hacia atrás en la historia.
 * 4. [BRECHA] Persistencia de CSV: La exportación genera un String (CSV); se recomienda el uso de 
 *    'FileProvider' o 'MediaStore' para guardar el archivo físicamente en el dispositivo.
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
     * AUDITORÍA: Carga Dinámica de Reportes.
     * Alterna entre API Semanal y Mensual basándose en el estado del 'period'.
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

    fun setServiceFilter(serviceId: String?) {
        _state.update { it.copy(selectedServiceId = serviceId) }
        loadReport()
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

    /**
     * AUDITORÍA: Exportación de Datos.
     * Solicita al backend el formato CSV del reporte actual.
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

    fun clearExport() {
        _state.update { it.copy(exportContent = null) }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}
