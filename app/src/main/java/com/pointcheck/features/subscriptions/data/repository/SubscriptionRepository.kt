package com.pointcheck.features.subscriptions.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.core.network.NetworkHandler
import com.pointcheck.features.subscriptions.data.dto.SubscriptionRequestDto
import com.pointcheck.features.subscriptions.data.dto.SubscriptionResponseDto
import retrofit2.Response

/**
 * Repositorio encargado de gestionar las operaciones de datos relacionadas con las suscripciones.
 * Se comunica con la API para obtener, crear y cancelar suscripciones de perfiles profesionales.
 *
 * @property api Servicio de API de Retrofit para realizar las peticiones de red.
 */
class SubscriptionRepository(private val api: ApiService) {

    /**
     * Obtiene la suscripción actual asociada a un perfil profesional específico.
     *
     * @param professionalProfileId El identificador único del perfil profesional.
     * @return [Result] que contiene [SubscriptionResponseDto] en caso de éxito, o una excepción en caso de error.
     * Si no se encuentra una suscripción activa, el error contiene el mensaje "NO_SUBSCRIPTION".
     */
    suspend fun getCurrentSubscriptionByProfessionalProfile(
        professionalProfileId: String
    ): Result<SubscriptionResponseDto> {
        return handleApiCall("No se encontró suscripción activa") { 
            api.getCurrentSubscriptionByProfessionalProfile(professionalProfileId) 
        }
    }

    /**
     * Crea una nueva suscripción para un profesional.
     *
     * @param request Objeto DTO con los detalles de la suscripción a crear (plan, fechas, etc.).
     * @return [Result] con los detalles de la suscripción creada [SubscriptionResponseDto].
     */
    suspend fun createSubscription(request: SubscriptionRequestDto): Result<SubscriptionResponseDto> {
        return handleApiCall("Error al crear suscripción") { api.createSubscription(request) }
    }

    /**
     * Cancela una suscripción existente.
     *
     * @param id El identificador único de la suscripción a cancelar.
     * @return [Result] con los detalles de la suscripción cancelada [SubscriptionResponseDto].
     */
    suspend fun cancelSubscription(id: String): Result<SubscriptionResponseDto> {
        return handleApiCall("Error al cancelar suscripción") { api.cancelSubscription(id) }
    }

    /**
     * Función auxiliar para manejar las llamadas a la API de forma genérica.
     * Gestiona la respuesta de red, códigos de error específicos (como 404) y excepciones.
     *
     * @param errorMsg Mensaje de error personalizado para mostrar en caso de fallo.
     * @param call Bloque de código suspendido que ejecuta la llamada a la API.
     * @return [Result] con el resultado de la operación.
     */
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
