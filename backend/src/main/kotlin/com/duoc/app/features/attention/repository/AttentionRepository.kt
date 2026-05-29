package com.duoc.app.features.attention.repository

import com.duoc.app.features.attention.model.Attention
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface AttentionRepository : JpaRepository<Attention, String> {
    fun findByReservation_Id(reservationId: String): Attention?
    fun existsByReservation_Id(reservationId: String): Boolean

    fun findBySpecialist_IdAndStartedAtBetween(
        specialistId: String,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<Attention>

    fun findByClient_Id(clientId: String): List<Attention>
    fun findBySpecialist_Id(specialistId: String?): List<Attention>
}
