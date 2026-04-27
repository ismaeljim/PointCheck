package com.pointcheck.features.dashboard.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.features.dashboard.data.dto.DashboardMetricsDto
import retrofit2.Response

class DashboardRepository(private val api: ApiService) {

    suspend fun getDashboardMetrics(userId: Long, role: String): Result<DashboardMetricsDto> {
        return try {
            val response = api.getDashboardMetrics(userId, role)
            if (response.isSuccessful) {
                Result.success(response.body() ?: DashboardMetricsDto())
            } else {
                Result.failure(Exception("Error al cargar métricas: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
