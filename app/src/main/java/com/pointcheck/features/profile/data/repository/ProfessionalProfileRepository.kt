package com.pointcheck.features.profile.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.core.network.NetworkHandler
import com.pointcheck.features.profile.data.dto.ProfessionalProfileRequestDto
import com.pointcheck.features.profile.data.dto.ProfessionalProfileResponseDto

/**
 * Repositorio encargado de la comunicación con el API para la gestión de Perfiles Profesionales.
 * Implementa el manejo de errores mediante el NetworkHandler del núcleo de la App.
 */
class ProfessionalProfileRepository(private val api: ApiService) {

    /**
     * Obtiene el perfil del especialista asociado al ID de usuario autenticado.
     */
    suspend fun getProfileByUserId(userId: String): Result<ProfessionalProfileResponseDto> {
        return try {
            val response = api.getProfessionalProfileByUserId(userId)
            NetworkHandler.handleResponse(response, "Error al obtener perfil profesional")
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }

    /**
     * Registra un nuevo perfil profesional para el especialista.
     */
    suspend fun createProfile(request: ProfessionalProfileRequestDto): Result<ProfessionalProfileResponseDto> {
        return try {
            val response = api.createProfessionalProfile(request)
            NetworkHandler.handleResponse(response, "Error al crear perfil profesional")
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }

    /**
     * Actualiza los datos comerciales, de ubicación o disponibilidad del especialista.
     */
    suspend fun updateProfile(id: String, request: ProfessionalProfileRequestDto): Result<ProfessionalProfileResponseDto> {
        return try {
            val response = api.updateProfessionalProfile(id, request)
            NetworkHandler.handleResponse(response, "Error al actualizar perfil profesional")
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }
}

