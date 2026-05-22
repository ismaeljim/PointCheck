package com.pointcheck.features.auth.data.repository

import com.pointcheck.core.network.ApiClient
import com.pointcheck.core.network.ApiService
import com.pointcheck.core.network.NetworkHandler
import com.pointcheck.features.auth.data.dto.LoginRequestDto
import com.pointcheck.features.auth.data.dto.RegisterRequestDto
import com.pointcheck.features.auth.data.dto.UserResponseDto

class AuthRepository(
    private val apiService: ApiService = ApiClient.instance
) {
    suspend fun login(email: String, password: String): Result<UserResponseDto> {
        return try {
            val response = apiService.login(LoginRequestDto(email, password))
            NetworkHandler.handleResponse(response, "Error al iniciar sesión")
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }

    suspend fun register(request: RegisterRequestDto): Result<UserResponseDto> {
        return try {
            val response = apiService.register(request)
            NetworkHandler.handleResponse(response, "Error al registrar usuario")
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }
}

