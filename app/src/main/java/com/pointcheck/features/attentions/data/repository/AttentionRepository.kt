package com.pointcheck.features.attentions.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.features.attentions.data.dto.AttentionRequestDto
import com.pointcheck.features.attentions.data.dto.AttentionResponseDto
import retrofit2.Response

class AttentionRepository(private val api: ApiService) {

    suspend fun getAttentionByReservation(reservationId: Long): Result<AttentionResponseDto> {
        return handleApiCall { api.getAttentionByReservation(reservationId) }
    }

    suspend fun startAttention(request: AttentionRequestDto): Result<AttentionResponseDto> {
        return handleApiCall { api.startAttention(request) }
    }

    suspend fun finishAttention(id: Long, observations: String): Result<AttentionResponseDto> {
        return handleApiCall { api.finishAttention(id, mapOf("observations" to observations)) }
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
                Result.failure(Exception("Error servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
