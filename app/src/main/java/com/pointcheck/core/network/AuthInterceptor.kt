package com.pointcheck.core.network

import com.pointcheck.core.prefs.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor de OkHttp de Grado Industrial.
 * 
 * Garantiza una comunicación Stateless con el Backend mediante la inyección
 * única y segura de la cabecera Authorization. Soporta tanto Basic Auth
 * para desarrollo/admin como Bearer para flujos JWT.
 */
class AuthInterceptor(private val userPreferences: UserPreferences) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            userPreferences.token.first()
        }

        val request = chain.request()
        
        // Si no hay token, registramos el fallo pero dejamos seguir (para Login/Register)
        if (token.isNullOrBlank()) {
            android.util.Log.w("AuthInterceptor", "Petición saliente sin TOKEN: ${request.url}")
            return chain.proceed(request)
        }

        val authHeaderValue = when {
            token.startsWith("Basic ", ignoreCase = true) -> token
            token.startsWith("Bearer ", ignoreCase = true) -> token
            else -> "Bearer $token"
        }

        android.util.Log.d("AuthInterceptor", "Inyectando cabecera Auth para: ${request.url}")

        val authenticatedRequest = request.newBuilder()
            .header("Authorization", authHeaderValue)
            .header("Accept", "application/json")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
