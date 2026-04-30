package com.pointcheck.features.subscriptions.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.features.subscriptions.data.dto.SubscriptionRequestDto
import com.pointcheck.features.subscriptions.data.dto.SubscriptionResponseDto
import retrofit2.Response

class SubscriptionRepository(private val api: ApiService) {

    suspend fun getCurrentSubscriptionByProfessionalProfile(
        professionalProfileId: Long
    ): Result<SubscriptionResponseDto> {
        return handleApiCall { api.getCurrentSubscriptionByProfessionalProfile(professionalProfileId) }
    }

    suspend fun createSubscription(request: SubscriptionRequestDto): Result<SubscriptionResponseDto> {
        return handleApiCall { api.createSubscription(request) }
    }

    suspend fun cancelSubscription(id: Long): Result<SubscriptionResponseDto> {
        return handleApiCall { api.cancelSubscription(id) }
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
            } else if (response.code() == 404) {
                Result.failure(Exception("NO_SUBSCRIPTION"))
            } else {
                Result.failure(Exception("Error servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
