package com.pointcheck.features.subscriptions.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.features.subscriptions.data.dto.SubscriptionRequestDto
import com.pointcheck.features.subscriptions.data.dto.SubscriptionResponseDto
import retrofit2.Response

class SubscriptionRepository(private val api: ApiService) {

    suspend fun getSubscriptionByProfileId(profileId: Long): Result<SubscriptionResponseDto> {
        return handleApiCall { api.getSubscriptionByProfileId(profileId) }
    }

    suspend fun createSubscription(request: SubscriptionRequestDto): Result<SubscriptionResponseDto> {
        return handleApiCall { api.createSubscription(request) }
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
