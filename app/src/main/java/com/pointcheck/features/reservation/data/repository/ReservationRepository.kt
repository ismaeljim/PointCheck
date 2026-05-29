package com.pointcheck.features.reservation.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.core.network.NetworkHandler
import com.pointcheck.features.reservation.data.dto.*
import com.pointcheck.features.services.data.dto.ServiceResponseDto
import com.pointcheck.features.profile.data.dto.ProfessionalProfileResponseDto
import retrofit2.Response

/**
 * Repositorio para gestionar las operaciones de reservas con el backend.
 * Actúa como única fuente de verdad para la UI.
 */
class ReservationRepository(private val api: ApiService) {

<<<<<<< Updated upstream
    suspend fun getReservationsByClient(clientId: Long): Result<List<ReservationResponseDto>> {
        return handleApiCall("Error al obtener reservas del cliente") { api.getReservationsByClient(clientId) }
    }

    suspend fun getUpcomingReservationsByClient(clientId: Long): Result<List<ReservationResponseDto>> {
        return handleApiCall("Error al obtener próximas reservas") { api.getUpcomingReservationsByClient(clientId) }
    }

    suspend fun getReservationHistoryByClient(clientId: Long): Result<List<ReservationResponseDto>> {
        return handleApiCall("Error al obtener historial de reservas") { api.getReservationHistoryByClient(clientId) }
    }

    suspend fun getReservationsBySpecialist(specialistId: Long): Result<List<ReservationResponseDto>> {
        return handleApiCall("Error al obtener reservas del especialista") { api.getReservationsBySpecialist(specialistId) }
    }

    suspend fun getTodayReservationsBySpecialist(specialistId: Long): Result<List<ReservationResponseDto>> {
        return handleApiCall("Error al obtener reservas de hoy") { api.getTodayReservationsBySpecialist(specialistId) }
=======
    // CAMBIO DE NOMBRE: De 'getReservationsByClient' a 'getReservationHistoryByClient'
    // Esto quita el error en AppointmentHistoryViewModel
    suspend fun getReservationHistoryByClient(clientId: String): Result<List<ReservationResponseDto>> {
        return handleApiCall { api.getReservationsByClient(clientId) }
    }

    suspend fun getUpcomingReservationsByClient(clientId: String): Result<List<ReservationResponseDto>> {
        return handleApiCall { api.getUpcomingReservationsByClient(clientId) }
    }

    suspend fun getReservationsBySpecialist(specialistId: String): Result<List<ReservationResponseDto>> {
        return handleApiCall { api.getReservationsBySpecialist(specialistId) }
    }

    suspend fun getTodayReservationsBySpecialist(specialistId: String): Result<List<ReservationResponseDto>> {
        return handleApiCall { api.getTodayReservationsBySpecialist(specialistId) }
>>>>>>> Stashed changes
    }

    suspend fun createReservation(request: ReservationRequestDto): Result<ReservationResponseDto> {
        return handleApiCall("Error al crear reserva") { api.createReservation(request) }
    }

<<<<<<< Updated upstream
    suspend fun updateReservationStatus(id: Long, status: String): Result<ReservationResponseDto> {
        return handleApiCall("Error al actualizar estado de reserva") { api.updateReservationStatus(id, ReservationStatusUpdateRequestDto(status)) }
    }

    suspend fun cancelReservation(id: Long): Result<ReservationResponseDto> {
        return handleApiCall("Error al cancelar reserva") { api.cancelReservation(id) }
=======
    // ID como String para UUID
    suspend fun updateReservationStatus(id: String, status: String): Result<ReservationResponseDto> {
        return handleApiCall { api.updateReservationStatus(id, ReservationStatusUpdateRequestDto(status)) }
    }

    // ID como String para UUID
    suspend fun cancelReservation(id: String): Result<ReservationResponseDto> {
        return handleApiCall { api.cancelReservation(id) }
>>>>>>> Stashed changes
    }

    suspend fun getActiveProfiles(categoryId: Long? = null): Result<List<SpecialistResponseDto>> {
        return handleApiCall("Error al obtener perfiles activos") { api.getActiveProfessionalProfiles(categoryId) }
    }

<<<<<<< Updated upstream
    suspend fun getServices(professionalProfileId: Long): Result<List<ServiceResponseDto>> {
        return handleApiCall("Error al obtener servicios") { api.getServicesByProfessionalProfileId(professionalProfileId) }
=======
    // ID como String para UUID
    suspend fun getServices(professionalProfileId: String): Result<List<ServiceResponseDto>> {
        return handleApiCall { api.getServicesByProfessionalProfileId(professionalProfileId) }
>>>>>>> Stashed changes
    }

    suspend fun getWeather(city: String): Result<com.pointcheck.features.external.data.dto.WeatherResponseDto> {
        return handleApiCall("Error al obtener clima") { api.getWeather(city) }
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

