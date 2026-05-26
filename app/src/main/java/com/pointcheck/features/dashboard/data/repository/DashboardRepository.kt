package com.pointcheck.features.dashboard.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.core.network.NetworkHandler
import com.pointcheck.features.dashboard.data.dto.DashboardMetricsDto
import com.pointcheck.features.dashboard.data.dto.ReportSummaryResponseDto
import com.pointcheck.features.dashboard.data.dto.WeeklyReportResponseDto
import retrofit2.Response

class DashboardRepository(private val api: ApiService) {

    suspend fun getDashboardMetrics(userId: Long, role: String): Result<DashboardMetricsDto> {
        return handleApiCall("Error al obtener métricas") { api.getDashboardMetrics(userId, role) }
    }

    suspend fun getReportSummaryBySpecialist(specialistId: Long): Result<ReportSummaryResponseDto> {
        return handleApiCall("Error al obtener resumen de reporte") { api.getReportSummaryBySpecialist(specialistId) }
    }

    suspend fun getWeeklyReport(userId: Long, weekOffset: Int): Result<WeeklyReportResponseDto> {
        return handleApiCall("Error al obtener reporte semanal") { api.getWeeklyReport(userId, weekOffset) }
    }

    suspend fun getMonthlyReport(userId: Long, monthOffset: Int): Result<com.pointcheck.features.dashboard.data.dto.MonthlyReportResponseDto> {
        return handleApiCall("Error al obtener reporte mensual") { api.getMonthlyReport(userId, monthOffset) }
    }

    suspend fun getClientDashboard(clientId: Long): Result<com.pointcheck.features.dashboard.data.dto.ClientDashboardResponseDto> {
        return handleApiCall("Error al obtener dashboard de cliente") { api.getClientDashboard(clientId) }
    }

    suspend fun getWeather(city: String): Result<com.pointcheck.features.external.data.dto.WeatherResponseDto> {
        return handleApiCall("Error al obtener clima") { api.getWeather(city) }
    }

    suspend fun markNotificationAsRead(notificationId: Long): Result<Unit> {
        return try {
            val response = api.markNotificationAsRead(notificationId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                NetworkHandler.handleResponse(response, "Error al marcar notificación")
                Result.success(Unit) // Response body for markNotificationAsRead is probably empty/Unit
            }
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }

    suspend fun exportWeeklyReport(userId: Long, weekOffset: Int): Result<String> {
        return try {
            val response = api.exportWeeklyReport(userId, weekOffset)
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

    suspend fun exportMonthlyReport(userId: Long, monthOffset: Int): Result<String> {
        return try {
            val response = api.exportMonthlyReport(userId, monthOffset)
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

    suspend fun getAllUsers(): Result<List<com.pointcheck.features.auth.data.dto.UserResponseDto>> {
        return handleApiCall("Error al obtener usuarios") { api.getAllUsers() }
    }

    suspend fun toggleUserStatus(userId: Long): Result<com.pointcheck.features.auth.data.dto.UserResponseDto> {
        return handleApiCall("Error al cambiar estado de usuario") { api.toggleUserStatus(userId) }
    }

    suspend fun getFinancialReport(): Result<Map<String, Any>> {
        return handleApiCall("Error al obtener reporte financiero") { api.getFinancialReport() }
    }

    suspend fun getSettings(): Result<List<com.pointcheck.features.dashboard.data.dto.GlobalSettingDto>> {
        return handleApiCall("Error al obtener configuraciones") { api.getSettings() }
    }

    suspend fun updateSetting(key: String, value: String): Result<com.pointcheck.features.dashboard.data.dto.GlobalSettingDto> {
        return handleApiCall("Error al actualizar configuración") { api.updateSetting(key, value) }
    }

    suspend fun getAuditLogs(): Result<List<com.pointcheck.features.admin.data.dto.AuditLogDto>> {
        return handleApiCall("Error al obtener logs de auditoría") { api.getAuditLogs() }
    }

    private suspend fun <T> handleApiCall(errorMsg: String, call: suspend () -> Response<T>): Result<T> {
        return try {
            val response = call()
            NetworkHandler.handleResponse(response, errorMsg)
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }
}

