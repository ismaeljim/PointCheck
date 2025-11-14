package com.pointcheck.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reservations")
data class Reservation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userEmail: String,
    val name: String,
    val epochMillis: Long,
    val createdAt: Long = System.currentTimeMillis()
)

