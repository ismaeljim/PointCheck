package com.pointcheck.features.dashboard.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.features.dashboard.data.dto.DashboardMetricsDto
import com.pointcheck.features.dashboard.data.dto.ReportSummaryResponseDto
import retrofit2.Response

class DashboardRepository(private val api: ApiService) {

    suspend fun getDashboardMetrics(userId: Long, role: String): Result<DashboardMetricsDto> {
        return handleApiCall { api.getDashboardMetrics(userId, role) }
    }

    suspend fun getReportSummaryBySpecialist(specialistId: Long): Result<ReportSummaryResponseDto> {
        return handleApiCall { api.getReportSummaryBySpecialist(specialistId) }
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
