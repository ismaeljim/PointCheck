package com.pointcheck.features.billing.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.features.billing.data.dto.BillingRecordRequestDto
import com.pointcheck.features.billing.data.dto.BillingRecordResponseDto
import com.pointcheck.features.billing.data.dto.MarkAsPaidRequestDto
import retrofit2.Response

class BillingRepository(private val api: ApiService) {

    suspend fun createBillingRecord(request: BillingRecordRequestDto): Result<BillingRecordResponseDto> {
        return handleApiCall { api.createBillingRecord(request) }
    }

    suspend fun markAsPaid(
        id: Long,
        paymentMethod: String,
        externalReference: String? = null,
        notes: String? = null
    ): Result<BillingRecordResponseDto> {
        val request = MarkAsPaidRequestDto(paymentMethod, externalReference, notes)
        return handleApiCall { api.markBillingAsPaid(id, request) }
    }

    suspend fun cancelBillingRecord(id: Long): Result<BillingRecordResponseDto> {
        return handleApiCall { api.cancelBillingRecord(id) }
    }

    suspend fun getBillingBySpecialist(specialistId: Long): Result<List<BillingRecordResponseDto>> {
        return handleApiCall { api.getBillingBySpecialist(specialistId) }
    }

    suspend fun getPendingBillingBySpecialist(specialistId: Long): Result<List<BillingRecordResponseDto>> {
        return handleApiCall { api.getPendingBillingBySpecialist(specialistId) }
    }

    suspend fun getTodayBillingBySpecialist(specialistId: Long): Result<List<BillingRecordResponseDto>> {
        return handleApiCall { api.getTodayBillingBySpecialist(specialistId) }
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
