package com.pointcheck.core.network

import com.pointcheck.features.auth.data.User
import com.pointcheck.features.booking.data.Reservation
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- User Endpoints ---
    @POST("api/users/login")
    suspend fun login(@Body user: User): Response<User>

    @POST("api/users/register")
    suspend fun registerUser(@Body user: User): Response<User>

    // --- Reservation Endpoints ---
    @GET("api/reservations")
    suspend fun getReservationsByEmail(@Query("email") email: String): Response<List<Reservation>>

    @POST("api/reservations")
    suspend fun createReservation(@Body reservation: Reservation): Response<Reservation>

    @DELETE("api/reservations/{id}")
    suspend fun deleteReservation(@Path("id") id: Int): Response<Void>

    // --- External API Endpoints ---
    @GET("api/external/weather/{city}")
    suspend fun getWeather(@Path("city") city: String): Response<Any>

    @GET("api/external/place/{placeId}")
    suspend fun getPlaceDetails(@Path("placeId") placeId: String): Response<Any>
}
