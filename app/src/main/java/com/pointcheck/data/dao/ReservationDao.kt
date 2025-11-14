package com.pointcheck.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pointcheck.model.Reservation
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservationDao {
    @Query("SELECT * FROM reservations WHERE userEmail = :userEmail ORDER BY epochMillis ASC")
    fun getReservationsByUser(userEmail: String): Flow<List<Reservation>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservation(reservation: Reservation)
    
    @Query("DELETE FROM reservations WHERE id = :id")
    suspend fun deleteReservation(id: Long)
    
    @Query("SELECT * FROM reservations WHERE userEmail = :userEmail AND epochMillis >= :currentTime ORDER BY epochMillis ASC")
    fun getUpcomingReservations(userEmail: String, currentTime: Long): Flow<List<Reservation>>
}

