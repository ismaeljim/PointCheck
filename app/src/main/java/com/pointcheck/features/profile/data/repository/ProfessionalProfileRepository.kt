package com.pointcheck.features.profile.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.features.profile.data.dto.ProfessionalProfileRequestDto
import com.pointcheck.features.profile.data.dto.ProfessionalProfileResponseDto

class ProfessionalProfileRepository(private val api: ApiService) {

    suspend fun getProfileByUserId(userId: Long): Result<ProfessionalProfileResponseDto> {
        return try {
            val response = api.getProfessionalProfileByUserId(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener perfil profesional"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createProfile(request: ProfessionalProfileRequestDto): Result<ProfessionalProfileResponseDto> {
        return try {
            val response = api.createProfessionalProfile(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al crear perfil profesional"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(id: Long, request: ProfessionalProfileRequestDto): Result<ProfessionalProfileResponseDto> {
        return try {
            val response = api.updateProfessionalProfile(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al actualizar perfil profesional"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
