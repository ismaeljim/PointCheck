package com.duoc.app.repository

import com.duoc.app.model.Reservation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReservationRepository : JpaRepository<Reservation, Long> {
    // Spring creará una consulta para buscar todas las reservas de un email específico
    fun findByUserEmail(userEmail: String): List<Reservation>
}
    