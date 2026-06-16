package com.pointcheck.features.reservation.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.core.network.NetworkHandler
import com.pointcheck.features.reservation.data.dto.*
import com.pointcheck.features.services.data.dto.ServiceResponseDto
import com.pointcheck.features.profile.data.dto.ProfessionalProfileResponseDto
import retrofit2.Response

/**
 * Repositorio para gestionar las operaciones de reservas con el backend.
 * Actúa como única fuente de verdad para la UI en lo que respecta a la gestión de citas y turnos.
 *
 * @property api Servicio de API de Retrofit para realizar las peticiones de red.
 */
class ReservationRepository(private val api: ApiService) {

    /**
     * Obtiene la lista completa de reservas asociadas a un cliente.
     *
     * @param clientId Identificador único del cliente.
     * @return [Result] con la lista de [ReservationResponseDto].
     */
    suspend fun getReservationsByClient(clientId: String): Result<List<ReservationResponseDto>> {
        return handleApiCall("Error al obtener reservas del cliente") { api.getReservationsByClient(clientId) }
    }

    /**
     * Obtiene las reservas futuras (pendientes o confirmadas) de un cliente.
     *
     * @param clientId Identificador único del cliente.
     * @return [Result] con la lista de reservas próximas.
     */
    suspend fun getUpcomingReservationsByClient(clientId: String): Result<List<ReservationResponseDto>> {
        return handleApiCall("Error al obtener próximas reservas") { api.getUpcomingReservationsByClient(clientId) }
    }

    /**
     * Obtiene el historial de reservas pasadas o finalizadas de un cliente.
     *
     * @param clientId Identificador único del cliente.
     * @return [Result] con la lista de reservas históricas.
     */
    suspend fun getReservationHistoryByClient(clientId: String): Result<List<ReservationResponseDto>> {
        return handleApiCall("Error al obtener historial de reservas") { api.getReservationHistoryByClient(clientId) }
    }

    /**
     * Obtiene todas las reservas asignadas a un especialista/profesional.
     *
     * @param specialistId Identificador único del perfil profesional.
     * @return [Result] con la lista de reservas del especialista.
     */
    suspend fun getReservationsBySpecialist(specialistId: String): Result<List<ReservationResponseDto>> {
        return handleApiCall("Error al obtener reservas del especialista") { api.getReservationsBySpecialist(specialistId) }
    }

    /**
     * Obtiene las reservas programadas para el día actual para un especialista.
     *
     * @param specialistId Identificador único del perfil profesional.
     * @return [Result] con la lista de reservas de hoy.
     */
    suspend fun getTodayReservationsBySpecialist(specialistId: String): Result<List<ReservationResponseDto>> {
        return handleApiCall("Error al obtener reservas de hoy") { api.getTodayReservationsBySpecialist(specialistId) }
    }

    /**
     * Crea una nueva reserva en el sistema.
     *
     * @param request Objeto DTO con los detalles de la reserva (servicio, fecha, hora, etc.).
     * @return [Result] con los detalles de la reserva creada.
     */
    suspend fun createReservation(request: ReservationRequestDto): Result<ReservationResponseDto> {
        return handleApiCall("Error al crear reserva") { api.createReservation(request) }
    }

    /**
     * Actualiza el estado de una reserva existente (ej: de PENDING a CONFIRMED).
     *
     * @param id Identificador único de la reserva.
     * @param status Nuevo estado a asignar.
     * @return [Result] con la reserva actualizada.
     */
    suspend fun updateReservationStatus(id: String, status: String): Result<ReservationResponseDto> {
        return handleApiCall("Error al actualizar estado de reserva") { api.updateReservationStatus(id, ReservationStatusUpdateRequestDto(status)) }
    }

    /**
     * Cancela una reserva.
     *
     * @param userId ID del usuario que solicita la cancelación (Seguridad Anti-IDOR).
     * @param id Identificador único de la reserva a cancelar.
     * @return [Result] con la reserva cancelada.
     */
    suspend fun cancelReservation(userId: String, id: String): Result<ReservationResponseDto> {
        return handleApiCall("Error al cancelar reserva") { api.cancelReservation(userId, id) }
    }

    /**
     * Confirma que el pago de una reserva ha sido realizado.
     *
     * @param userId ID del usuario que confirma el pago.
     * @param id Identificador único de la reserva.
     * @return [Result] con la reserva actualizada.
     */
    suspend fun confirmPayment(userId: String, id: String): Result<ReservationResponseDto> {
        return handleApiCall("Error al confirmar pago") { api.confirmPayment(userId, id) }
    }

    /**
     * Obtiene una lista de perfiles profesionales activos, opcionalmente filtrados por categoría.
     *
     * @param categoryId ID opcional de la categoría para filtrar especialistas.
     * @return [Result] con la lista de especialistas encontrados.
     */
    suspend fun getActiveProfiles(categoryId: String? = null): Result<List<SpecialistResponseDto>> {
        return handleApiCall("Error al obtener perfiles activos") { api.getActiveProfessionalProfiles(categoryId) }
    }

    /**
     * Obtiene los servicios ofrecidos por un profesional específico.
     *
     * @param professionalProfileId Identificador del perfil profesional.
     * @return [Result] con la lista de servicios.
     */
    suspend fun getServices(professionalProfileId: String): Result<List<ServiceResponseDto>> {
        return handleApiCall("Error al obtener servicios") { api.getServicesByProfessionalProfileId(professionalProfileId) }
    }

    /**
     * Consulta los horarios disponibles para un especialista en una fecha determinada.
     *
     * @param specialistId Identificador del profesional.
     * @param date Fecha a consultar en formato ISO.
     * @return [Result] con la disponibilidad horaria.
     */
    suspend fun getAvailability(specialistId: String, date: String): Result<AvailabilityResponseDto> {
        return handleApiCall("Error al obtener disponibilidad") { api.getAvailability(specialistId, date) }
    }

    /**
     * Obtiene información climática para una ciudad específica (utilizado para recomendaciones en reservas al aire libre).
     *
     * @param city Nombre de la ciudad.
     * @return [Result] con los datos del clima.
     */
    suspend fun getWeather(city: String): Result<com.pointcheck.features.external.data.dto.WeatherResponseDto> {
        return handleApiCall("Error al obtener clima") { api.getWeather(city) }
    }

    /**
     * Obtiene todas las reservas del sistema (Solo ADMIN).
     */
    suspend fun getAllReservations(): Result<List<ReservationResponseDto>> {
        return handleApiCall("Error al obtener todas las reservas") { api.getAllReservations() }
    }

    /**
     * Función genérica para manejar llamadas a la API y centralizar el tratamiento de errores.
     */
    private suspend fun <T> handleApiCall(errorMsg: String, call: suspend () -> Response<T>): Result<T> {
        return try {
            val response = call()
            NetworkHandler.handleResponse(response, errorMsg)
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }
}
