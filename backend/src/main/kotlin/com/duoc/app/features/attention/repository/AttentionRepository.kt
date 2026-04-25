package com.duoc.app.features.attention.repository

import com.duoc.app.features.attention.model.Attention
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface AttentionRepository : JpaRepository<Attention, Long> {
    fun findByReservation_Id(reservationId: Long): Attention?
    fun existsByReservation_Id(reservationId: Long): Boolean
    
    fun findBySpecialist_IdAndStartedAtBetween(
        specialistId: Long,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<Attention>

    fun findByClient_Id(clientId: Long): List<Attention>
    fun findBySpecialist_Id(specialistId: Long): List<Attention>
}
