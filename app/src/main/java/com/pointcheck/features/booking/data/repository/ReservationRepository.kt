package com.pointcheck.features.booking.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.features.booking.data.dto.*
import retrofit2.Response

/**
 * Repositorio para gestionar las operaciones de reservas con el backend.
 * Actúa como única fuente de verdad para la UI.
 */
class ReservationRepository(private val api: ApiService) {

    suspend fun getReservationsByClient(clientId: Long): Result<List<ReservationResponseDto>> {
        return handleApiCall { api.getReservationsByClientId(clientId) }
    }

    suspend fun getReservationsBySpecialist(specialistId: Long): Result<List<ReservationResponseDto>> {
        return handleApiCall { api.getReservationsBySpecialistId(specialistId) }
    }

    suspend fun createReservation(request: ReservationRequestDto): Result<ReservationResponseDto> {
        return handleApiCall { api.createReservation(request) }
    }

    suspend fun deleteReservation(reservationId: Long): Result<Void> {
        return handleApiCall { api.deleteReservation(reservationId) }
    }

    suspend fun getSpecialists(): Result<List<SpecialistResponseDto>> {
        return handleApiCall { api.getSpecialists() }
    }

    suspend fun getServices(specialistId: Long): Result<List<ServiceResponseDto>> {
        return handleApiCall { api.getServicesBySpecialistId(specialistId) }
    }

    private suspend fun <T> handleApiCall(call: suspend () -> Response<T>): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null || response.code() == 204) {
                    Result.success(body as T)
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
