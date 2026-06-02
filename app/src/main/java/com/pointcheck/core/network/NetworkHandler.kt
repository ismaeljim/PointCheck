package com.pointcheck.core.network

import com.google.gson.Gson
import com.pointcheck.core.network.model.ApiErrorDto
import retrofit2.Response
import java.io.IOException

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
            Result.failure(Exception(message))
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
