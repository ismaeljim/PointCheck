package com.pointcheck.features.admin.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.core.network.NetworkHandler
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
            NetworkHandler.handleResponse(response, "Error al obtener usuarios")
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
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
            NetworkHandler.handleResponse(response, "Error al cambiar estado")
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }

    /**
     * Actualiza la información de un usuario desde el panel de administración.
     * 
     * @param userId ID del usuario a editar.
     * @param request Datos actualizados.
     * @return Result con el usuario actualizado.
     */
    suspend fun updateUser(userId: String, request: com.pointcheck.features.admin.data.dto.AdminUserUpdateRequestDto): Result<UserResponseDto> {
        return try {
            val response = apiService.updateAdminUser(userId, request)
            NetworkHandler.handleResponse(response, "Error al actualizar usuario")
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }

    /**
     * Obtiene los registros de auditoría históricos del sistema con paginación.
     * 
     * @param page Número de página a solicitar.
     * @return Result con el [AuditPageDto] o el error correspondiente.
     */
    suspend fun getAuditLogs(page: Int = 0): Result<com.pointcheck.features.admin.data.dto.AuditPageDto> {
        return try {
            val response = apiService.getAuditLogs(page)
            NetworkHandler.handleResponse(response, "Error al obtener logs de auditoría")
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }
}
