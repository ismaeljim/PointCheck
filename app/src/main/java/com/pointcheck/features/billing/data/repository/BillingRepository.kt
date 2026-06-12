package com.pointcheck.features.billing.data.repository

import com.pointcheck.core.network.ApiService
import com.pointcheck.core.network.NetworkHandler
import com.pointcheck.features.billing.data.dto.BillingRecordRequestDto
import com.pointcheck.features.billing.data.dto.BillingRecordResponseDto
import com.pointcheck.features.billing.data.dto.MarkAsPaidRequestDto
import retrofit2.Response

/**
 * Repositorio encargado de gestionar los registros de facturación y pagos.
 * Se comunica con la API para crear, actualizar y consultar movimientos financieros de los profesionales.
 *
 * @property api Servicio de API de Retrofit para realizar las peticiones de red.
 */
class BillingRepository(private val api: ApiService) {

    /**
     * Crea un nuevo registro de facturación en el sistema.
     *
     * @param request Datos del registro de facturación a crear.
     * @return [Result] con el registro de facturación creado [BillingRecordResponseDto].
     */
    suspend fun createBillingRecord(request: BillingRecordRequestDto): Result<BillingRecordResponseDto> {
        return handleApiCall("Error al crear cobro") { api.createBillingRecord(request) }
    }

    /**
     * Marca un registro de facturación como pagado.
     *
     * @param id Identificador único del registro de facturación.
     * @param paymentMethod Método de pago utilizado (ej: "CASH", "TRANSFER").
     * @param externalReference Referencia externa opcional (ej: número de transferencia).
     * @param notes Notas adicionales sobre el pago.
     * @return [Result] con el registro actualizado.
     */
    suspend fun markAsPaid(
        id: String,
        paymentMethod: String,
        externalReference: String? = null,
        notes: String? = null
    ): Result<BillingRecordResponseDto> {
        val request = MarkAsPaidRequestDto(paymentMethod, externalReference, notes)
        return handleApiCall("Error al registrar pago") { api.markBillingAsPaid(id, request) }
    }

    /**
     * Cancela un registro de facturación existente.
     *
     * @param id Identificador único del registro a cancelar.
     * @return [Result] con el registro cancelado.
     */
    suspend fun cancelBillingRecord(id: String): Result<BillingRecordResponseDto> {
        return handleApiCall("Error al cancelar cobro") { api.cancelBillingRecord(id) }
    }

    /**
     * Obtiene todos los registros de facturación asociados a un especialista.
     *
     * @param specialistId Identificador único del profesional.
     * @return [Result] con la lista de registros de facturación.
     */
    suspend fun getBillingBySpecialist(specialistId: String): Result<List<BillingRecordResponseDto>> {
        return handleApiCall("Error al obtener historial de cobros") { api.getBillingBySpecialist(specialistId) }
    }

    /**
     * Obtiene los registros de facturación con estado pendiente para un especialista.
     *
     * @param specialistId Identificador único del profesional.
     * @return [Result] con la lista de registros pendientes.
     */
    suspend fun getPendingBillingBySpecialist(specialistId: String): Result<List<BillingRecordResponseDto>> {
        return handleApiCall("Error al obtener cobros pendientes") { api.getPendingBillingBySpecialist(specialistId) }
    }

    /**
     * Obtiene los registros de facturación generados durante el día actual para un especialista.
     *
     * @param specialistId Identificador único del profesional.
     * @return [Result] con la lista de registros de hoy.
     */
    suspend fun getTodayBillingBySpecialist(specialistId: String): Result<List<BillingRecordResponseDto>> {
        return handleApiCall("Error al obtener cobros de hoy") { api.getTodayBillingBySpecialist(specialistId) }
    }

    /**
     * Función auxiliar genérica para manejar las llamadas a la API.
     */
    private suspend fun <T> handleApiCall(errorMsg: String = "Error de red", call: suspend () -> Response<T>): Result<T> {
        return try {
            val response = call()
            NetworkHandler.handleResponse(response, errorMsg)
        } catch (e: Exception) {
            NetworkHandler.handleException(e)
        }
    }
}
