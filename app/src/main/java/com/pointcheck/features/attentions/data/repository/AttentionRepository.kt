package com.pointcheck.features.attentions.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.core.network.NetworkHandler
import com.pointcheck.features.attentions.data.dto.StartAttentionRequestDto
import com.pointcheck.features.attentions.data.dto.FinishAttentionRequestDto
import com.pointcheck.features.attentions.data.dto.AttentionResponseDto
import retrofit2.Response

/**
 * Repositorio encargado de gestionar el flujo de las atenciones (atención de citas).
 * Permite iniciar, finalizar y consultar el historial de atenciones realizadas por los especialistas.
 *
 * @property api Servicio de API de Retrofit para realizar las peticiones de red.
 */
class AttentionRepository(private val api: ApiService) {

    /**
     * Registra el inicio de una atención asociada a una reserva específica.
     *
     * @param reservationId Identificador único de la reserva.
     * @param observations Observaciones iniciales opcionales sobre el estado o requerimientos.
     * @return [Result] con los detalles de la atención iniciada [AttentionResponseDto].
     */
    suspend fun startAttention(
        reservationId: String,
        observations: String? = null
    ): Result<AttentionResponseDto> {
        return handleApiCall("Error al iniciar atención") { 
            api.startAttention(StartAttentionRequestDto(reservationId, observations)) 
        }
    }

    /**
     * Registra la finalización de una atención en curso.
     *
     * @param attentionId Identificador único de la atención a cerrar.
     * @param observations Observaciones finales sobre el servicio realizado.
     * @param durationMinutes Duración efectiva del servicio en minutos.
     * @return [Result] con los detalles de la atención finalizada [AttentionResponseDto].
     */
    suspend fun finishAttention(
        attentionId: String,
        observations: String? = null,
        durationMinutes: Int? = null
    ): Result<AttentionResponseDto> {
        return handleApiCall("Error al finalizar atención") { 
            api.finishAttention(attentionId, FinishAttentionRequestDto(observations, durationMinutes))
        }
    }

    /**
     * Obtiene la lista de atenciones programadas o realizadas durante el día actual para un especialista.
     *
     * @param specialistId Identificador único del perfil profesional.
     * @return [Result] con la lista de atenciones de hoy.
     */
    suspend fun getTodayAttentionsBySpecialist(specialistId: String): Result<List<AttentionResponseDto>> {
        return handleApiCall("Error al obtener atenciones de hoy") { 
            api.getTodayAttentionsBySpecialist(specialistId) 
        }
    }

    /**
     * Obtiene el historial completo de atenciones recibidas por un cliente.
     *
     * @param clientId Identificador único del cliente.
     * @return [Result] con la lista de atenciones históricas.
     */
    suspend fun getAttentionHistoryByClient(clientId: String): Result<List<AttentionResponseDto>> {
        return handleApiCall("Error al obtener historial de atenciones") { 
            api.getAttentionHistoryByClient(clientId) 
        }
    }

    /**
     * Consulta la atención vinculada a una reserva específica.
     *
     * @param reservationId Identificador de la reserva.
     * @return [Result] con la atención.
     */
    suspend fun getAttentionByReservation(reservationId: String): Result<AttentionResponseDto> {
        return handleApiCall("Error al obtener atención por reserva") {
            api.getAttentionByReservation(reservationId)
        }
    }

    /**
     * Función genérica para manejar las llamadas a la API y centralizar el manejo de errores.
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

