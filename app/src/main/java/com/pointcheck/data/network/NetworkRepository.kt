package com.pointcheck.data.network

import com.pointcheck.model.Reservation
import com.pointcheck.model.User
import retrofit2.Response

class NetworkRepository {
    private val apiService = ApiClient.instance

    // --- User Functions ---
    suspend fun login(user: User): Response<User> {
        return apiService.login(user)
    }

    suspend fun registerUser(user: User): Response<User> {
        return apiService.registerUser(user)
    }

    // --- Reservation Functions ---
    suspend fun getReservationsByEmail(email: String): Response<List<Reservation>> {
        return apiService.getReservationsByEmail(email)
    }

    suspend fun createReservation(reservation: Reservation): Response<Reservation> {
        return apiService.createReservation(reservation)
    }

    suspend fun deleteReservation(id: Int): Response<Void> {
        return apiService.deleteReservation(id)
    }

    // --- External API Functions ---
    suspend fun getWeather(city: String): Response<Any> {
        return apiService.getWeather(city)
    }

    suspend fun getPlaceDetails(placeId: String): Response<Any> {
        return apiService.getPlaceDetails(placeId)
    }
}
