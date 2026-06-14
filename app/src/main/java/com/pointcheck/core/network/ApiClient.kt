package com.pointcheck.core.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.pointcheck.core.prefs.UserPreferences
import android.content.Context

/**
 * Cliente de API configurado con Retrofit para la comunicación con el backend.
 */
object ApiClient {
    
    private const val BASE_URL = "http://192.168.100.74:8080/"
    private lateinit var apiService: ApiService
    private lateinit var retrofit: Retrofit

    /**
     * Inicializa el servicio con soporte para autenticación JWT.
     */
    fun init(context: Context) {
        val userPrefs = UserPreferences(context)
        
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(userPrefs))
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    val instance: ApiService get() = apiService
    val retrofitInstance: Retrofit get() = retrofit
}
