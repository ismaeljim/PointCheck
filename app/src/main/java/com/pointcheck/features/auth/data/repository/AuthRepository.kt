package com.pointcheck.features.auth.data.repository

import com.pointcheck.core.network.ApiClient
import com.pointcheck.core.network.ApiService
import com.pointcheck.core.network.NetworkHandler
import com.pointcheck.features.auth.data.dto.LoginRequestDto
import com.pointcheck.features.auth.data.dto.RegisterRequestDto
import com.pointcheck.features.auth.data.dto.UserResponseDto

/**
 * Repositorio de Autenticación.
 * Gestiona la comunicación con el backend para los procesos de inicio de sesión y registro de usuarios.
 * Centraliza el manejo de respuestas de red y excepciones a través de [NetworkHandler].
 *
 * @property apiService Servicio de API de Retrofit para realizar las peticiones de autenticación.
 */
class AuthRepository(
    private val apiService: ApiService = ApiClient.instance
) {
    /**
     * Realiza el inicio de sesión del usuario utilizando sus credenciales.
     *
     * @param email Correo electrónico del usuario.
     * @param password Contraseña del usuario.
     * @return [Result] que contiene [UserResponseDto] si el inicio de sesión es exitoso, o una falla en caso contrario.
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
     * Registra un nuevo usuario en el sistema (Cliente o Profesional).
     *
     * @param request Objeto [RegisterRequestDto] con los datos necesarios para el registro.
     * @return [Result] que contiene [UserResponseDto] del usuario recién creado, o una falla.
     */
    suspend fun register(request: RegisterRequestDto): Result<UserResponseDto> {
        return try {
            val response = apiService.register(request)
            NetworkHandler.handleResponse(response, "Error al registrar usuario")
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }

    /**
     * Obtiene un usuario por su ID.
     */
    suspend fun getUserById(id: String): Result<UserResponseDto> {
        return try {
            val response = apiService.getUserById(id)
            NetworkHandler.handleResponse(response, "Error al obtener usuario")
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }

    /**
     * Actualiza la dirección del usuario.
     */
    suspend fun updateUserAddress(id: String, address: String): Result<UserResponseDto> {
        return try {
            val response = apiService.updateUserAddress(id, address)
            NetworkHandler.handleResponse(response, "Error al actualizar dirección")
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }

    /**
     * Actualiza el perfil completo del usuario.
     */
    suspend fun updateProfile(id: String, request: com.pointcheck.features.auth.data.dto.UserUpdateRequestDto): Result<UserResponseDto> {
        return try {
            val response = apiService.updateUserProfile(id, request)
            NetworkHandler.handleResponse(response, "Error al actualizar perfil")
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }

    /**
     * Cambia la contraseña del usuario.
     */
    suspend fun changePassword(id: String, current: String, new: String): Result<Unit> {
        return try {
            val request = com.pointcheck.features.auth.data.dto.ChangePasswordRequestDto(current, new)
            val response = apiService.changePassword(id, request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("La contraseña actual es incorrecta o los datos son inválidos"))
            }
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }
}

