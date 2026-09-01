package com.pointcheck.core.network

import android.content.Context
import com.pointcheck.core.prefs.UserPreferences
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente API centralizado para PointCheck.
 * Configurado para soportar conexión desde dispositivos físicos mediante IP local.
 */
object ApiClient {
    // NUEVA IP detectada por ipconfig (Red Hotspot/Móvil)
    private const val BASE_URL = "http://192.168.43.29:8080/"

    private var retrofit: Retrofit? = null

    /**
     * Inicializa el cliente con el contexto de la aplicación.
     * Se llama usualmente en el onCreate de MainActivity.
     */
    fun init(context: Context) {
        val userPreferences = UserPreferences(context)
        
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(AuthInterceptor(context, userPreferences))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    val retrofitInstance: Retrofit
        get() = retrofit ?: throw IllegalStateException("ApiClient must be initialized in MainActivity before use")

    val instance: ApiService
        get() = retrofitInstance.create(ApiService::class.java)
}
