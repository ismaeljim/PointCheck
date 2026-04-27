package com.pointcheck.core.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * ApiClient optimizado para la conexión con el backend Spring Boot.
 * Fuente de verdad: Backend Spring Boot (10.0.2.2:8080 para emulador).
 */
object ApiClient {
    
    // BASE_URL para emulador Android apuntando a localhost del PC
    private const val BASE_URL = "http://10.0.2.2:8080/"
    
    // TODO: Implementar interceptor para JWT si el backend lo requiere en el futuro
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val instance: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
