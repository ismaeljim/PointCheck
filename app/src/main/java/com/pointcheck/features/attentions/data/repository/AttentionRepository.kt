package com.pointcheck.features.attentions.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.core.network.NetworkHandler
import com.pointcheck.features.attentions.data.dto.StartAttentionRequestDto
import com.pointcheck.features.attentions.data.dto.FinishAttentionRequestDto
import com.pointcheck.features.attentions.data.dto.AttentionResponseDto
import retrofit2.Response

class AttentionRepository(private val api: ApiService) {

    suspend fun startAttention(
        reservationId: String,
        observations: String? = null
    ): Result<AttentionResponseDto> {
        return handleApiCall("Error al iniciar atención") { 
            api.startAttention(StartAttentionRequestDto(reservationId, observations)) 
        }
    }

    suspend fun finishAttention(
        attentionId: String,
        observations: String? = null
    ): Result<AttentionResponseDto> {
        return handleApiCall("Error al finalizar atención") { 
            api.finishAttention(attentionId, FinishAttentionRequestDto(observations)) 
        }
    }

    suspend fun getTodayAttentionsBySpecialist(specialistId: String): Result<List<AttentionResponseDto>> {
        return handleApiCall("Error al obtener atenciones de hoy") { 
            api.getTodayAttentionsBySpecialist(specialistId) 
        }
    }

    suspend fun getAttentionHistoryByClient(clientId: String): Result<List<AttentionResponseDto>> {
        return handleApiCall("Error al obtener historial de atenciones") { 
            api.getAttentionHistoryByClient(clientId) 
        }
    }

    private suspend fun <T> handleApiCall(errorMsg: String, call: suspend () -> Response<T>): Result<T> {
        return try {
            val response = call()
            NetworkHandler.handleResponse(response, errorMsg)
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }
}
// Repositorio corregido y limpio de conflictos de Git.
