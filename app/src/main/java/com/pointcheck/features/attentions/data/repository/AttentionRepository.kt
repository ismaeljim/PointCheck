package com.pointcheck.features.attentions.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.features.attentions.data.dto.StartAttentionRequestDto
import com.pointcheck.features.attentions.data.dto.FinishAttentionRequestDto
import com.pointcheck.features.attentions.data.dto.AttentionResponseDto
import retrofit2.Response

class AttentionRepository(private val api: ApiService) {

    suspend fun startAttention(
        reservationId: Long,
        observations: String? = null
    ): Result<AttentionResponseDto> {
        return handleApiCall { 
            api.startAttention(StartAttentionRequestDto(reservationId, observations)) 
        }
    }

    suspend fun finishAttention(
        attentionId: Long,
        observations: String? = null
    ): Result<AttentionResponseDto> {
        return handleApiCall { 
            api.finishAttention(attentionId, FinishAttentionRequestDto(observations)) 
        }
    }

    suspend fun getTodayAttentionsBySpecialist(specialistId: Long): Result<List<AttentionResponseDto>> {
        return handleApiCall { api.getTodayAttentionsBySpecialist(specialistId) }
    }

    suspend fun getAttentionHistoryByClient(clientId: Long): Result<List<AttentionResponseDto>> {
        return handleApiCall { api.getAttentionHistoryByClient(clientId) }
    }

    private suspend fun <T> handleApiCall(call: suspend () -> Response<T>): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Cuerpo de respuesta vacío"))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error servidor: ${response.code()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
