package com.pointcheck.features.dashboard.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.core.network.NetworkHandler
import com.pointcheck.features.dashboard.data.dto.DashboardMetricsDto
import com.pointcheck.features.dashboard.data.dto.ReportSummaryResponseDto
import com.pointcheck.features.dashboard.data.dto.WeeklyReportResponseDto
import com.pointcheck.features.dashboard.data.dto.MonthlyReportResponseDto
import retrofit2.Response

/**
 * Repositorio encargado de gestionar la obtención de métricas, reportes y configuraciones del Dashboard.
 * Centraliza la comunicación con los endpoints de analítica y administración.
 *
 * @property api Servicio de API de Retrofit para realizar las peticiones de red.
 */
class DashboardRepository(private val api: ApiService) {

    /**
     * Obtiene las métricas principales del dashboard.
     * La identidad y el rol se resuelven en el backend mediante el token de autenticación.
     *
     * @return [Result] con el objeto [DashboardMetricsDto].
     */
    suspend fun getDashboardMetrics(): Result<DashboardMetricsDto> {
        return handleApiCall("Error al obtener métricas") { api.getDashboardMetrics() }
    }

    /**
     * Obtiene un resumen estadístico de la actividad para un especialista.
     *
     * @param specialistId Identificador único del perfil profesional.
     * @return [Result] con el resumen del reporte [ReportSummaryResponseDto].
     */
    suspend fun getReportSummaryBySpecialist(specialistId: String): Result<ReportSummaryResponseDto> {
        return handleApiCall("Error al obtener resumen de reporte") { api.getReportSummaryBySpecialist(specialistId) }
    }

    /**
     * Obtiene el reporte de actividad semanal.
     *
     * @param userId Identificador del usuario.
     * @param weekOffset Desplazamiento de semanas (0 para la actual, -1 para la anterior).
     * @param serviceId Opcional, para filtrar por un servicio específico.
     * @return [Result] con los datos del reporte semanal.
     */
    suspend fun getWeeklyReport(userId: String, weekOffset: Int, serviceId: String? = null): Result<WeeklyReportResponseDto> {
        return handleApiCall("Error al obtener reporte semanal") { api.getWeeklyReport(userId, weekOffset, serviceId) }
    }

    /**
     * Obtiene el reporte de actividad mensual.
     *
     * @param userId Identificador del usuario.
     * @param monthOffset Desplazamiento de meses (0 para el actual).
     * @param serviceId Opcional, para filtrar por un servicio específico.
     * @return [Result] con los datos del reporte mensual.
     */
    suspend fun getMonthlyReport(userId: String, monthOffset: Int, serviceId: String? = null): Result<MonthlyReportResponseDto> {
        return handleApiCall("Error al obtener reporte mensual") { api.getMonthlyReport(userId, monthOffset, serviceId) }
    }

    /**
     * Obtiene los datos del dashboard específicos para la vista del cliente.
     *
     * @param clientId Identificador del cliente.
     * @return [Result] con el resumen del dashboard del cliente.
     */
    suspend fun getClientDashboard(clientId: String): Result<com.pointcheck.features.dashboard.data.dto.ClientDashboardResponseDto> {
        return handleApiCall("Error al obtener dashboard de cliente") { api.getClientDashboard(clientId) }
    }

    /**
     * Obtiene información climática de una ciudad.
     */
    suspend fun getWeather(city: String): Result<com.pointcheck.features.external.data.dto.WeatherResponseDto> {
        return handleApiCall("Error al obtener clima") { api.getWeather(city) }
    }

    /**
     * Marca una notificación como leída en el sistema.
     *
     * @param notificationId Identificador de la notificación.
     * @return [Result] con Unit si fue exitoso.
     */
    suspend fun markNotificationAsRead(notificationId: String): Result<Unit> {
        return try {
            val response = api.markNotificationAsRead(notificationId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                NetworkHandler.handleResponse(response, "Error al marcar notificación")
                Result.success(Unit) 
            }
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }

    /**
     * Exporta el reporte semanal en formato de texto o binario (según API).
     */
    suspend fun exportWeeklyReport(userId: String, weekOffset: Int, serviceId: String? = null): Result<String> {
        return try {
            val response = api.exportWeeklyReport(userId, weekOffset, serviceId)
            if (response.isSuccessful) {
                Result.success(response.body()?.string() ?: "")
            } else {
                val result = NetworkHandler.handleResponse(response, "Error al exportar reporte semanal")
                Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }

    /**
     * Exporta el reporte mensual.
     */
    suspend fun exportMonthlyReport(userId: String, monthOffset: Int, serviceId: String? = null): Result<String> {
        return try {
            val response = api.exportMonthlyReport(userId, monthOffset, serviceId)
            if (response.isSuccessful) {
                Result.success(response.body()?.string() ?: "")
            } else {
                val result = NetworkHandler.handleResponse(response, "Error al exportar reporte mensual")
                Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }

    // --- Admin Functions ---

    /**
     * Obtiene la lista de todos los usuarios registrados (solo Admin).
     */
    suspend fun getAllUsers(): Result<List<com.pointcheck.features.auth.data.dto.UserResponseDto>> {
        return handleApiCall("Error al obtener usuarios") { api.getAllUsers() }
    }

    /**
     * Activa o desactiva un usuario.
     */
    suspend fun toggleUserStatus(userId: String): Result<com.pointcheck.features.auth.data.dto.UserResponseDto> {
        return handleApiCall("Error al cambiar estado de usuario") { api.toggleUserStatus(userId) }
    }

    /**
     * Obtiene un reporte financiero global.
     */
    suspend fun getFinancialReport(): Result<Map<String, Any>> {
        return handleApiCall("Error al obtener reporte financiero") { api.getFinancialReport() }
    }

    /**
     * Obtiene la configuración global del sistema.
     */
    suspend fun getSettings(): Result<List<com.pointcheck.features.dashboard.data.dto.GlobalSettingDto>> {
        return handleApiCall("Error al obtener configuraciones") { api.getSettings() }
    }

    /**
     * Actualiza una entrada de la configuración global.
     */
    suspend fun updateSetting(key: String, value: String): Result<com.pointcheck.features.dashboard.data.dto.GlobalSettingDto> {
        return handleApiCall("Error al actualizar configuración") { api.updateSetting(key, value) }
    }

    /**
     * Obtiene los logs de auditoría del sistema.
     */
    suspend fun getAuditLogs(): Result<List<com.pointcheck.features.admin.data.dto.AuditLogDto>> {
        return handleApiCall("Error al obtener logs de auditoría") { api.getAuditLogs() }
    }

    /**
     * Manejador genérico de llamadas a la API.
     */
    private suspend fun <T> handleApiCall(errorMsg: String, call: suspend () -> Response<T>): Result<T> {
        return try {
            val response = call()
            NetworkHandler.handleResponse(response, errorMsg)
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }
}
