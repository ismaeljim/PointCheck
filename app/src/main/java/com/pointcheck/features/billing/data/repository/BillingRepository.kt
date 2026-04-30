package com.pointcheck.features.billing.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.features.billing.data.dto.BillingRecordRequestDto
import com.pointcheck.features.billing.data.dto.BillingRecordResponseDto
import retrofit2.Response

class BillingRepository(private val api: ApiService) {

    suspend fun getBillingByReservation(reservationId: Long): Result<BillingRecordResponseDto> {
        return handleApiCall { api.getBillingByReservation(reservationId) }
    }

    suspend fun createBillingRecord(request: BillingRecordRequestDto): Result<BillingRecordResponseDto> {
        return handleApiCall { api.createBillingRecord(request) }
    }

    suspend fun updateBillingStatus(id: Long, status: String): Result<BillingRecordResponseDto> {
        return handleApiCall { api.updateBillingStatus(id, status) }
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
