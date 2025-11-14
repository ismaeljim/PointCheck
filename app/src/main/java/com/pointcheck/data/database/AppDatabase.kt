package com.pointcheck.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pointcheck.data.dao.ReservationDao
import com.pointcheck.data.dao.UserDao
import com.pointcheck.model.Reservation
import com.pointcheck.model.User

@Database(
    entities = [User::class, Reservation::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun reservationDao(): ReservationDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pointcheck_database"
                )
                .fallbackToDestructiveMigration() // Para desarrollo - permite migración automática
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

