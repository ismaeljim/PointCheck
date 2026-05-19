package com.pointcheck.features.reservation.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.features.reservation.data.dto.*
import com.pointcheck.features.services.data.dto.ServiceResponseDto
import com.pointcheck.features.profile.data.dto.ProfessionalProfileResponseDto
import retrofit2.Response

/**
 * Repositorio para gestionar las operaciones de reservas con el backend.
 * Actúa como única fuente de verdad para la UI.
 */
class ReservationRepository(private val api: ApiService) {

    suspend fun getReservationsByClient(clientId: Long): Result<List<ReservationResponseDto>> {
        return handleApiCall { api.getReservationsByClient(clientId) }
    }

    suspend fun getUpcomingReservationsByClient(clientId: Long): Result<List<ReservationResponseDto>> {
        return handleApiCall { api.getUpcomingReservationsByClient(clientId) }
    }

    suspend fun getReservationHistoryByClient(clientId: Long): Result<List<ReservationResponseDto>> {
        return handleApiCall { api.getReservationHistoryByClient(clientId) }
    }

    suspend fun getReservationsBySpecialist(specialistId: Long): Result<List<ReservationResponseDto>> {
        return handleApiCall { api.getReservationsBySpecialist(specialistId) }
    }

    suspend fun getTodayReservationsBySpecialist(specialistId: Long): Result<List<ReservationResponseDto>> {
        return handleApiCall { api.getTodayReservationsBySpecialist(specialistId) }
    }

    suspend fun createReservation(request: ReservationRequestDto): Result<ReservationResponseDto> {
        return handleApiCall { api.createReservation(request) }
    }

    suspend fun updateReservationStatus(id: Long, status: String): Result<ReservationResponseDto> {
        return handleApiCall { api.updateReservationStatus(id, ReservationStatusUpdateRequestDto(status)) }
    }

    suspend fun cancelReservation(id: Long): Result<ReservationResponseDto> {
        return handleApiCall { api.cancelReservation(id) }
    }

    suspend fun getActiveProfiles(): Result<List<SpecialistResponseDto>> {
        return handleApiCall { api.getActiveProfessionalProfiles() }
    }

    suspend fun getServices(professionalProfileId: Long): Result<List<ServiceResponseDto>> {
        return handleApiCall { api.getServicesByProfessionalProfileId(professionalProfileId) }
    }

    suspend fun getWeather(city: String): Result<com.pointcheck.features.external.data.dto.WeatherResponseDto> {
        return handleApiCall { api.getWeather(city) }
    }

    private suspend fun <T> handleApiCall(call: suspend () -> Response<T>): Result<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null || response.code() == 204) {
                    @Suppress("UNCHECKED_CAST")
                    Result.success(body as T)
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
