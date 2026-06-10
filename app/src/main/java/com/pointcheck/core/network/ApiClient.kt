package com.pointcheck.core.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente de API configurado con Retrofit para la comunicación con el backend.
 * Proporciona una instancia única ([ApiService]) para realizar peticiones de red.
 *
 * Fuente de verdad: Backend Spring Boot alojado localmente.
 */
object ApiClient {
    
    /** URL base para el emulador de Android apuntando al localhost del PC. */
    private const val BASE_URL = "http://10.0.2.2:8080/"
    
    /** Configuración del cliente HTTP con tiempos de espera personalizados. */
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /** Instancia privada de Retrofit. */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /** Proporciona la instancia de Retrofit configurada. */
    val retrofitInstance: Retrofit by lazy {
        retrofit
    }

    /** Proporciona la implementación de los servicios definidos en [ApiService]. */
    val instance: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
