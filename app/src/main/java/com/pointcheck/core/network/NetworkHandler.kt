package com.pointcheck.core.network

import com.google.gson.Gson
import com.pointcheck.core.network.model.ApiErrorDto
import retrofit2.Response
import java.io.IOException

/**
 * Excepción personalizada para errores de API que incluye el código de estado HTTP.
 * Se ha blindado el parámetro message para evitar crashes por nulos desde el backend.
 */
class ApiException(
    override val message: String? = "Error de comunicación con el servidor",
    val code: Int = 0
) : Exception(message)

/**
 * Objeto utilitario encargado de centralizar el manejo de respuestas de red y excepciones.
 */
object NetworkHandler {
    private val gson = Gson()

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
            // Ahora devolvemos una ApiException con el código real (404, 401, 500, etc)
            Result.failure(ApiException(message = message, code = response.code()))
        }
    }

    fun handleException(e: Exception): Result<Nothing> {
        val message = when (e) {
            is IOException -> "No se pudo conectar al servidor. Revisa tu internet."
            else -> "Ocurrió un error inesperado: ${e.localizedMessage}"
        }
        return Result.failure(Exception(message))
    }
}
