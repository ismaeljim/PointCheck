package com.duoc.app.features.attention.repository

import com.duoc.app.features.attention.model.Attention
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface AttentionRepository : JpaRepository<Attention, Long> {
    fun findByReservationId(reservationId: Long): Attention?
    fun existsByReservationId(reservationId: Long): Boolean
    
    fun findBySpecialistIdAndStartedAtBetween(
        specialistId: Long,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<Attention>

    fun findByClientId(clientId: Long): List<Attention>
    fun findBySpecialistId(specialistId: Long): List<Attention>
}
