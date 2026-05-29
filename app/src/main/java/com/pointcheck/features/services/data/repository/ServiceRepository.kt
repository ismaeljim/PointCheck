package com.pointcheck.features.services.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.core.network.NetworkHandler
import com.pointcheck.features.services.data.dto.ServiceRequestDto
import com.pointcheck.features.services.data.dto.ServiceResponseDto

class ServiceRepository(private val api: ApiService) {

    suspend fun getServices(professionalProfileId: String): Result<List<ServiceResponseDto>> {
        return try {
            val response = api.getServicesByProfessionalProfileId(professionalProfileId)
            NetworkHandler.handleResponse(response, "Error al obtener servicios")
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }

    suspend fun createService(request: ServiceRequestDto): Result<ServiceResponseDto> {
        return try {
            val response = api.createService(request)
            NetworkHandler.handleResponse(response, "Error al crear servicio")
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }

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

