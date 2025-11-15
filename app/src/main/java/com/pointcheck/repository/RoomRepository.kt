package com.pointcheck.repository

import android.app.Application
import com.pointcheck.data.database.AppDatabase
import com.pointcheck.model.Reservation
import com.pointcheck.model.User
import kotlinx.coroutines.flow.Flow

class RoomRepository(app: Application) {
    private val database = AppDatabase.getDatabase(app)
    private val userDao = database.userDao()
    private val reservationDao = database.reservationDao()

    suspend fun registerUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun findUserByEmail(email: String): User? {
        return userDao.findUserByEmail(email)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(email: String) {
        userDao.deleteUser(email)
    }

    fun getReservationsByUser(email: String): Flow<List<Reservation>> {
        return reservationDao.getReservationsByUser(email)
    }

    suspend fun insertReservation(reservation: Reservation) {
        reservationDao.insertReservation(reservation)
    }

    suspend fun updateReservation(reservation: Reservation) {
        reservationDao.updateReservation(reservation)
    }

    suspend fun deleteReservation(id: Int) {
        reservationDao.deleteReservation(id)
    }

    fun getUpcomingReservations(email: String): Flow<List<Reservation>> {
        return reservationDao.getUpcomingReservations(email, System.currentTimeMillis())
    }

    fun getReservationById(id: Int): Flow<Reservation?> {
        return reservationDao.getReservationById(id)
    }
}
