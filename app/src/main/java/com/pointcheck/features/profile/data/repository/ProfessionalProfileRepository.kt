package com.pointcheck.features.profile.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.core.network.NetworkHandler
import com.pointcheck.features.profile.data.dto.ProfessionalProfileRequestDto
import com.pointcheck.features.profile.data.dto.ProfessionalProfileResponseDto

/**
 * Repositorio encargado de la comunicación con el API para la gestión de Perfiles Profesionales.
 * Implementa el manejo de errores centralizado a través de [NetworkHandler].
 *
 * @property api Servicio de API de Retrofit para realizar las peticiones de red.
 */
class ProfessionalProfileRepository(private val api: ApiService) {

    /**
     * Obtiene el perfil del profesional asociado a un ID de usuario específico.
     *
     * @param userId Identificador único del usuario autenticado.
     * @return [Result] con el perfil profesional [ProfessionalProfileResponseDto].
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
     * Registra un nuevo perfil profesional en el sistema.
     *
     * @param request Objeto [ProfessionalProfileRequestDto] con la información del perfil a crear.
     * @return [Result] con el perfil profesional creado.
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
     * Actualiza los datos de un perfil profesional existente.
     * Permite modificar información comercial, ubicación, disponibilidad, etc.
     *
     * @param id Identificador único del perfil profesional a actualizar.
     * @param request Objeto [ProfessionalProfileRequestDto] con los nuevos datos.
     * @return [Result] con el perfil profesional actualizado.
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

