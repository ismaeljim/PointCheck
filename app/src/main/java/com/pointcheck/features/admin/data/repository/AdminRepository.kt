package com.pointcheck.features.admin.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.features.admin.data.dto.AuditLogDto
import com.pointcheck.features.auth.data.dto.UserResponseDto

/**
 * Repositorio encargado de las operaciones de administración del sistema.
 * Conecta las vistas de administración con el servicio de red para gestionar usuarios y auditoría.
 * 
 * @property apiService Servicio de API para realizar las peticiones al backend.
 */
class AdminRepository(private val apiService: ApiService) {

    /**
     * Obtiene la lista completa de usuarios registrados en el sistema.
     * Requiere privilegios de administrador en el backend.
     * 
     * @return Result con la lista de [UserResponseDto] o el error correspondiente.
     */
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

    /**
     * Cambia el estado de activación (habilitado/deshabilitado) de un usuario.
     * 
     * @param userId Identificador único del usuario a modificar.
     * @return Result con los datos actualizados del usuario o el error.
     */
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

    /**
     * Obtiene los registros de auditoría históricos del sistema.
     * 
     * @return Result con la lista de [AuditLogDto] o el error correspondiente.
     */
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
