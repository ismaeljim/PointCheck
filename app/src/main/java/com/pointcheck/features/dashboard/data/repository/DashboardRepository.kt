package com.pointcheck.features.dashboard.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.features.dashboard.data.dto.DashboardMetricsDto
import com.pointcheck.features.dashboard.data.dto.ReportSummaryResponseDto
import com.pointcheck.features.dashboard.data.dto.WeeklyReportResponseDto
import retrofit2.Response

class DashboardRepository(private val api: ApiService) {

    suspend fun getDashboardMetrics(userId: Long, role: String): Result<DashboardMetricsDto> {
        return handleApiCall { api.getDashboardMetrics(userId, role) }
    }

    suspend fun getReportSummaryBySpecialist(specialistId: Long): Result<ReportSummaryResponseDto> {
        return handleApiCall { api.getReportSummaryBySpecialist(specialistId) }
    }

    suspend fun getWeeklyReport(userId: Long, weekOffset: Int): Result<WeeklyReportResponseDto> {
        return handleApiCall { api.getWeeklyReport(userId, weekOffset) }
    }

    suspend fun getMonthlyReport(userId: Long, monthOffset: Int): Result<com.pointcheck.features.dashboard.data.dto.MonthlyReportResponseDto> {
        return handleApiCall { api.getMonthlyReport(userId, monthOffset) }
    }

    suspend fun getClientDashboard(clientId: Long): Result<com.pointcheck.features.dashboard.data.dto.ClientDashboardResponseDto> {
        return handleApiCall { api.getClientDashboard(clientId) }
    }

    suspend fun getWeather(city: String): Result<com.pointcheck.features.external.data.dto.WeatherResponseDto> {
        return handleApiCall { api.getWeather(city) }
    }

    suspend fun markNotificationAsRead(notificationId: Long): Result<Unit> {
        return try {
            val response = api.markNotificationAsRead(notificationId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al marcar como leída: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportWeeklyReport(userId: Long, weekOffset: Int): Result<String> {
        return try {
            val response = api.exportWeeklyReport(userId, weekOffset)
            if (response.isSuccessful) {
                Result.success(response.body()?.string() ?: "")
            } else {
                Result.failure(Exception("Error servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportMonthlyReport(userId: Long, monthOffset: Int): Result<String> {
        return try {
            val response = api.exportMonthlyReport(userId, monthOffset)
            if (response.isSuccessful) {
                Result.success(response.body()?.string() ?: "")
            } else {
                Result.failure(Exception("Error servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun <T> handleApiCall(call: suspend () -> Response<T>): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Cuerpo de respuesta vacío"))
                }
            } else {
                Result.failure(Exception("Error servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
