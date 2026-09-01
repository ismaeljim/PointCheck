package com.pointcheck.core.network

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.pointcheck.core.prefs.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Interceptor de OkHttp con Reintento Elástico y Cerrojo Anti-Tormenta.
 * 
 * Implementa un mecanismo de absorción de latencia para evitar cierres de sesión falsos positivos
 * durante navegación intensiva o ráfagas de peticiones paralelas.
 */
class AuthInterceptor(
    private val context: Context,
    private val userPreferences: UserPreferences
) : Interceptor {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private val isPurging = AtomicBoolean(false)
        
        // Rutas secundarias que no deben gatillar purga de sesión aunque retornen 401
        private val EXCLUDED_FROM_PURGE = listOf(
            "/api/users/",
            "/api/dashboard/metrics",
            "/api/notifications/unread"
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath
        
        // 1. Intento inicial con el token disponible
        var token = userPreferences.getSyncToken()
        var response = chain.proceed(buildAuthenticatedRequest(originalRequest, token))

        val isSecondaryEndpoint = EXCLUDED_FROM_PURGE.any { path.contains(it) }

        // 2. Mecanismo de Reintento Elástico ante 401 (Principio de Tolerancia a Fallos)
        if (response.code == 401 && !isSecondaryEndpoint && !token.isNullOrBlank()) {
            Log.w("AuthInterceptor", "Fallo 401 transitorio detectado en $path. Ejecutando reintento elástico...")
            
            response.close() // Liberamos la conexión anterior

            // Pausa síncrona corta para permitir que otros procesos (como un refresco en curso) terminen
            try { Thread.sleep(100) } catch (e: InterruptedException) { }

            // Re-leemos el token fresco (pudo haber cambiado por otra petición paralela)
            val freshToken = userPreferences.getSyncToken()
            
            // Reintentamos la petición original con el token actualizado
            response = chain.proceed(buildAuthenticatedRequest(originalRequest, freshToken))

            // 3. Purga Condicional Estricta
            // Si después del reintento sigue fallando con 401, la identidad es definitivamente inválida.
            if (response.code == 401) {
                if (isPurging.compareAndSet(false, true)) {
                    Log.e("AuthInterceptor", "Identidad inválida confirmada tras reintento en $path. Iniciando purga.")
                    
                    scope.launch {
                        userPreferences.clearSession()
                    }

                    Handler(Looper.getMainLooper()).post {
                        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        context.startActivity(intent)
                        
                        // Enfriamiento del cerrojo para evitar bucles de reinicio
                        Handler(Looper.getMainLooper()).postDelayed({
                            isPurging.set(false)
                        }, 5000)
                    }
                }
            }
        }

        return response
    }

    /**
     * Construye una nueva petición inyectando el token de autenticación si está presente.
     */
    private fun buildAuthenticatedRequest(request: Request, token: String?): Request {
        if (token.isNullOrBlank() || token == "null") return request

        val authHeaderValue = if (token.startsWith("Bearer ", true) || token.startsWith("Basic ", true)) {
            token
        } else {
            "Bearer $token"
        }

        return request.newBuilder()
            .header("Authorization", authHeaderValue)
            .header("Accept", "application/json")
            .build()
    }
}
