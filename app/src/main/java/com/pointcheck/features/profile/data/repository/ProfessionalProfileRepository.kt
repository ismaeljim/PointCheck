package com.pointcheck.features.profile.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.core.network.NetworkHandler
import com.pointcheck.features.profile.data.dto.ProfessionalProfileRequestDto
import com.pointcheck.features.profile.data.dto.ProfessionalProfileResponseDto

class ProfessionalProfileRepository(private val api: ApiService) {

    suspend fun getProfileByUserId(userId: String): Result<ProfessionalProfileResponseDto> {
        return try {
            val response = api.getProfessionalProfileByUserId(userId)
            NetworkHandler.handleResponse(response, "Error al obtener perfil profesional")
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }

    suspend fun createProfile(request: ProfessionalProfileRequestDto): Result<ProfessionalProfileResponseDto> {
        return try {
            val response = api.createProfessionalProfile(request)
            NetworkHandler.handleResponse(response, "Error al crear perfil profesional")
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }

    suspend fun updateProfile(id: String, request: ProfessionalProfileRequestDto): Result<ProfessionalProfileResponseDto> {
        return try {
            val response = api.updateProfessionalProfile(id, request)
            NetworkHandler.handleResponse(response, "Error al actualizar perfil profesional")
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }
}

