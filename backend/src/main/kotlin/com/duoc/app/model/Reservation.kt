package com.duoc.app.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "reservations")
data class Reservation(
    @Id // Marca el 'id' como la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Le dice a la DB que genere el ID automáticamente
    val id: Long = 0,
    val userEmail: String,
    val name: String,
    val epochMillis: Long,
    val createdAt: Long = System.currentTimeMillis()
)
    