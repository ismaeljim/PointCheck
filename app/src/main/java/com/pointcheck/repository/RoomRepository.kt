package com.pointcheck.repository

import android.app.Application
import android.util.Log
import com.pointcheck.data.database.AppDatabase
import com.pointcheck.data.network.NetworkRepository
import com.pointcheck.model.Reservation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class RoomRepository(app: Application) {
    private val database = AppDatabase.getDatabase(app)
    private val reservationDao = database.reservationDao()
    private val networkRepository = NetworkRepository()

    // Obtiene las reservas de la caché local, pero también las refresca desde la red
    fun getUpcomingReservations(userEmail: String): Flow<List<Reservation>> {
        return reservationDao.getUpcomingReservations(userEmail, System.currentTimeMillis())
            .map { localReservations ->
                // En paralelo, pedimos los datos al servidor
                try {
                    val networkResponse = networkRepository.getReservationsByEmail(userEmail)
                    if (networkResponse.isSuccessful) {
                        val networkReservations = networkResponse.body() ?: emptyList()
                        // Borramos la caché vieja y la reemplazamos con los datos frescos del servidor
                        reservationDao.deleteAllFromUser(userEmail)
                        networkReservations.forEach { reservationDao.insertReservation(it) }
                    }
                } catch (e: Exception) {
                    Log.e("RoomRepository", "Error al refrescar reservas: ", e)
                }
                localReservations // Devolvemos los datos locales inmediatamente
            }
    }

    // Inserta primero en la red, y si tiene éxito, en la caché local
    suspend fun insertReservation(reservation: Reservation) {
        try {
            val response = networkRepository.createReservation(reservation)
            if (response.isSuccessful) {
                response.body()?.let { reservationDao.insertReservation(it) }
            }
        } catch (e: Exception) {
            Log.e("RoomRepository", "Error al insertar reserva: ", e)
        }
    }

    // Borra primero de la red, y si tiene éxito, de la caché local
    suspend fun deleteReservation(id: Int) {
        try {
            val response = networkRepository.deleteReservation(id)
            if (response.isSuccessful) {
                reservationDao.deleteReservation(id)
            }
        } catch (e: Exception) {
            Log.e("RoomRepository", "Error al borrar reserva: ", e)
        }
    }
    
    // Las funciones de obtener por ID y actualizar siguen usando la caché local por simplicidad
    fun getReservationById(id: Int): Flow<Reservation?> {
        return reservationDao.getReservationById(id)
    }

    suspend fun updateReservation(reservation: Reservation) {
        // En un futuro, aquí también se implementaría la lógica de red
        reservationDao.updateReservation(reservation)
    }
}
