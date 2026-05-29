package com.pointcheck.features.subscriptions.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.core.network.NetworkHandler
import com.pointcheck.features.subscriptions.data.dto.SubscriptionRequestDto
import com.pointcheck.features.subscriptions.data.dto.SubscriptionResponseDto
import retrofit2.Response

class SubscriptionRepository(private val api: ApiService) {

    suspend fun getCurrentSubscriptionByProfessionalProfile(
        professionalProfileId: String
    ): Result<SubscriptionResponseDto> {
        return handleApiCall("No se encontró suscripción activa") { 
            api.getCurrentSubscriptionByProfessionalProfile(professionalProfileId) 
        }
    }

    suspend fun createSubscription(request: SubscriptionRequestDto): Result<SubscriptionResponseDto> {
        return handleApiCall("Error al crear suscripción") { api.createSubscription(request) }
    }

<<<<<<< Updated upstream
    suspend fun cancelSubscription(id: Long): Result<SubscriptionResponseDto> {
        return handleApiCall("Error al cancelar suscripción") { api.cancelSubscription(id) }
=======
    suspend fun cancelSubscription(id: String): Result<SubscriptionResponseDto> {
        return handleApiCall { api.cancelSubscription(id) }
>>>>>>> Stashed changes
    }

    private suspend fun <T> handleApiCall(errorMsg: String, call: suspend () -> Response<T>): Result<T> {
        return try {
            val response = call()
            if (response.code() == 404) {
                Result.failure(Exception("NO_SUBSCRIPTION"))
            } else {
                NetworkHandler.handleResponse(response, errorMsg)
            }
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }
}

