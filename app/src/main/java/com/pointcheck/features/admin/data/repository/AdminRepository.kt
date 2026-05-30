package com.pointcheck.features.admin.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.features.admin.data.dto.AuditLogDto
import com.pointcheck.features.auth.data.dto.UserResponseDto

class AdminRepository(private val apiService: ApiService) {

    suspend fun getAllUsers(): Result<List<UserResponseDto>> {
        return try {
            val response = apiService.getAllUsers()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error al obtener usuarios: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleUserStatus(userId: String): Result<UserResponseDto> {
        return try {
            val response = apiService.toggleUserStatus(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al cambiar estado: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAuditLogs(): Result<List<AuditLogDto>> {
        return try {
            val response = apiService.getAuditLogs()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error al obtener logs: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
