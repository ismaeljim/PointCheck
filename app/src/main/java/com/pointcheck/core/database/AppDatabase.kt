package com.pointcheck.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pointcheck.features.auth.data.User

/**
 * AppDatabase se mantiene estrictamente como CACHÉ LOCAL opcional (Offline Indicator).
 * La fuente de verdad es el backend Spring Boot (MySQL).
 * Se han eliminado los DAOs para forzar el uso de Repositorios con API.
 */
@Database(
    entities = [User::class],
    version = 4, // Incrementada versión por eliminación de Reservation entity
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    // DAOs eliminados para desacoplar la lógica de negocio de la persistencia local.
    // Si se requiere caché en el futuro, implementar DAOs específicos para caché.

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
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
