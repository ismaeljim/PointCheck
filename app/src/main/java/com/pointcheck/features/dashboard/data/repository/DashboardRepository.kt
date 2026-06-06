package com.pointcheck.features.dashboard.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.core.network.NetworkHandler
import com.pointcheck.features.dashboard.data.dto.DashboardMetricsDto
import com.pointcheck.features.dashboard.data.dto.ReportSummaryResponseDto
import com.pointcheck.features.dashboard.data.dto.WeeklyReportResponseDto
import com.pointcheck.features.dashboard.data.dto.MonthlyReportResponseDto
import retrofit2.Response

class DashboardRepository(private val api: ApiService) {

    suspend fun getDashboardMetrics(userId: String, role: String): Result<DashboardMetricsDto> {
        return handleApiCall("Error al obtener métricas") { api.getDashboardMetrics(userId, role) }
    }

    suspend fun getReportSummaryBySpecialist(specialistId: String): Result<ReportSummaryResponseDto> {
        return handleApiCall("Error al obtener resumen de reporte") { api.getReportSummaryBySpecialist(specialistId) }
    }

    suspend fun getWeeklyReport(userId: String, weekOffset: Int, serviceId: String? = null): Result<WeeklyReportResponseDto> {
        return handleApiCall("Error al obtener reporte semanal") { api.getWeeklyReport(userId, weekOffset, serviceId) }
    }

    suspend fun getMonthlyReport(userId: String, monthOffset: Int, serviceId: String? = null): Result<MonthlyReportResponseDto> {
        return handleApiCall("Error al obtener reporte mensual") { api.getMonthlyReport(userId, monthOffset, serviceId) }
    }

    suspend fun getClientDashboard(clientId: String): Result<com.pointcheck.features.dashboard.data.dto.ClientDashboardResponseDto> {
        return handleApiCall("Error al obtener dashboard de cliente") { api.getClientDashboard(clientId) }
    }

    suspend fun getWeather(city: String): Result<com.pointcheck.features.external.data.dto.WeatherResponseDto> {
        return handleApiCall("Error al obtener clima") { api.getWeather(city) }
    }

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

    suspend fun getAllUsers(): Result<List<com.pointcheck.features.auth.data.dto.UserResponseDto>> {
        return handleApiCall("Error al obtener usuarios") { api.getAllUsers() }
    }

    suspend fun toggleUserStatus(userId: String): Result<com.pointcheck.features.auth.data.dto.UserResponseDto> {
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

    suspend fun getGlobalWeeklyReservations(): Result<List<com.pointcheck.features.reservation.data.dto.ReservationResponseDto>> {
        return handleApiCall("Error al obtener citas semanales") { api.getGlobalWeeklyReservations() }
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
