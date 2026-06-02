package com.pointcheck.features.auth.data.repository

import com.pointcheck.core.network.ApiClient
import com.pointcheck.core.network.ApiService
import com.pointcheck.core.network.NetworkHandler
import com.pointcheck.features.auth.data.dto.LoginRequestDto
import com.pointcheck.features.auth.data.dto.RegisterRequestDto
import com.pointcheck.features.auth.data.dto.UserResponseDto

/**
 * Repositorio de Autenticación.
 * Gestiona la comunicación con el backend para los procesos de Login y Registro.
 * 
 * AUDITORÍA:
 * - Se delega el manejo de errores a NetworkHandler.
 * - Centraliza las llamadas a la API mediante ApiService.
 */
class AuthRepository(
    private val apiService: ApiService = ApiClient.instance
) {
    /**
     * Realiza el inicio de sesión del usuario.
     */
    suspend fun login(email: String, password: String): Result<UserResponseDto> {
        return try {
            val response = apiService.login(LoginRequestDto(email, password))
            NetworkHandler.handleResponse(response, "Error al iniciar sesión")
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }

    /**
     * Registra un nuevo usuario (Cliente o Especialista).
     */
    suspend fun register(request: RegisterRequestDto): Result<UserResponseDto> {
        return try {
            val response = apiService.register(request)
            NetworkHandler.handleResponse(response, "Error al registrar usuario")
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }
}

