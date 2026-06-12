package com.pointcheck.core.network

import com.pointcheck.core.prefs.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor de OkHttp que adjunta el token JWT a las peticiones salientes.
 * 
 * Este componente es vital para la seguridad ISO 27001, ya que asegura que
 * todas las peticiones a endpoints protegidos lleven la credencial necesaria
 * sin que el desarrollador tenga que añadirla manualmente en cada llamada.
 */
class AuthInterceptor(private val userPreferences: UserPreferences) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Obtenemos el token de forma síncrona para el interceptor
        val token = runBlocking {
            userPreferences.token.first()
        }

        val requestBuilder = chain.request().newBuilder()
        
        // Si existe un token, lo añadimos a la cabecera Authorization
        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}
