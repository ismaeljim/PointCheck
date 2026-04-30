package com.pointcheck.features.auth.data.repository

import com.pointcheck.core.network.ApiClient
import com.pointcheck.core.network.ApiService
import com.pointcheck.features.auth.data.dto.LoginRequestDto
import com.pointcheck.features.auth.data.dto.RegisterRequestDto
import com.pointcheck.features.auth.data.dto.UserResponseDto

class AuthRepository(
    private val apiService: ApiService = ApiClient.instance
) {
    suspend fun login(email: String, password: String): Result<UserResponseDto> {
        return try {
            val response = apiService.login(LoginRequestDto(email, password))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error de autenticación (${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(request: RegisterRequestDto): Result<UserResponseDto> {
        return try {
            val response = apiService.register(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error en el registro (${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
