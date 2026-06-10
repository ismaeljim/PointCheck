package com.pointcheck.features.services.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.core.network.NetworkHandler
import com.pointcheck.features.services.data.dto.ServiceRequestDto
import com.pointcheck.features.services.data.dto.ServiceResponseDto

/**
 * Repositorio encargado de gestionar los servicios ofrecidos por los profesionales.
 * Permite listar, crear y eliminar servicios a través de la API.
 *
 * @property api Servicio de API de Retrofit para realizar las peticiones de red.
 */
class ServiceRepository(private val api: ApiService) {

    /**
     * Obtiene la lista de servicios asociados a un perfil profesional.
     *
     * @param professionalProfileId Identificador único del perfil profesional.
     * @return [Result] con la lista de [ServiceResponseDto].
     */
    suspend fun getServices(professionalProfileId: String): Result<List<ServiceResponseDto>> {
        return try {
            val response = api.getServicesByProfessionalProfileId(professionalProfileId)
            NetworkHandler.handleResponse(response, "Error al obtener servicios")
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }

    /**
     * Registra un nuevo servicio para el profesional autenticado.
     *
     * @param request Datos del servicio a crear (nombre, descripción, precio, duración).
     * @return [Result] con el servicio creado [ServiceResponseDto].
     */
    suspend fun createService(request: ServiceRequestDto): Result<ServiceResponseDto> {
        return try {
            val response = api.createService(request)
            NetworkHandler.handleResponse(response, "Error al crear servicio")
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }

    /**
     * Elimina un servicio existente del catálogo del profesional.
     *
     * @param id Identificador único del servicio a eliminar.
     * @return [Result.success] con [Unit] si la operación fue exitosa.
     */
    suspend fun deleteService(id: String): Result<Unit> {
        return try {
            val response = api.deleteService(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val result = NetworkHandler.handleResponse(response, "Error al eliminar servicio")
                Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }
}

