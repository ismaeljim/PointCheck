package com.pointcheck.core.network

import com.google.gson.Gson
import com.pointcheck.core.network.model.ApiErrorDto
import retrofit2.Response
import java.io.IOException

/**
 * Objeto utilitario encargado de centralizar el manejo de respuestas de red y excepciones.
 * Transforma las respuestas de Retrofit ([Response]) en el tipo [Result] estándar de Kotlin,
 * abstrayendo la lógica de parseo de errores del backend.
 */
object NetworkHandler {
    private val gson = Gson()

    /**
     * Procesa una respuesta de Retrofit y la convierte en un [Result].
     * Si la respuesta es exitosa, devuelve el cuerpo; de lo contrario, intenta extraer el mensaje
     * de error enviado por el servidor o utiliza un mensaje por defecto.
     *
     * @param T Tipo de dato esperado en la respuesta.
     * @param response Objeto [Response] devuelto por Retrofit.
     * @param defaultMsg Mensaje de error genérico en caso de que el servidor no proporcione uno específico.
     * @return [Result] con el éxito o fallo de la operación.
     */
    fun <T> handleResponse(response: Response<T>, defaultMsg: String): Result<T> {
        return if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            val errorBody = response.errorBody()?.string()
            val message = try {
                val apiError = gson.fromJson(errorBody, ApiErrorDto::class.java)
                apiError.message
            } catch (e: Exception) {
                "$defaultMsg (${response.code()})"
            }
            Result.failure(Exception(message))
        }
    }

    /**
     * Maneja excepciones lanzadas durante la ejecución de una llamada de red.
     * Distingue entre errores de conectividad ([IOException]) y errores inesperados.
     *
     * @param e La excepción capturada.
     * @return [Result.failure] con un mensaje descriptivo para el usuario.
     */
    fun handleException(e: Exception): Result<Nothing> {
        val message = when (e) {
            is IOException -> "No se pudo conectar al servidor. Revisa tu internet."
            else -> "Ocurrió un error inesperado: ${e.localizedMessage}"
        }
        return Result.failure(Exception(message))
    }
}
