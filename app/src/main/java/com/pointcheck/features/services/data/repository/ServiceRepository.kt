package com.pointcheck.features.services.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.features.services.data.dto.ServiceRequestDto
import com.pointcheck.features.services.data.dto.ServiceResponseDto

class ServiceRepository(private val api: ApiService) {

    suspend fun getServices(profileId: Long): Result<List<ServiceResponseDto>> {
        return try {
            val response = api.getServicesBySpecialistId(profileId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener servicios"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createService(request: ServiceRequestDto): Result<ServiceResponseDto> {
        return try {
            val response = api.createService(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al crear servicio"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteService(id: Long): Result<Unit> {
        return try {
            val response = api.deleteService(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al eliminar servicio"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
