package com.duoc.app.features.attention.repository

import com.duoc.app.features.attention.model.Attention
import com.duoc.app.features.attention.model.AttentionStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface AttentionRepository : JpaRepository<Attention, String> {
    
    @EntityGraph(attributePaths = ["client", "specialist", "reservation"])
    fun findByReservation_Id(reservationId: String): Attention?
    
    fun existsByReservation_Id(reservationId: String): Boolean

    @EntityGraph(attributePaths = ["client", "specialist", "reservation"])
    fun findBySpecialist_IdAndStartedAtBetween(
        specialistProfileId: String,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<Attention>

    fun countBySpecialist_IdAndStartedAtBetweenAndStatus(
        specialistProfileId: String,
        start: LocalDateTime,
        end: LocalDateTime,
        status: AttentionStatus
    ): Long

    @Query("SELECT COALESCE(SUM(a.durationMinutes), 0) FROM Attention a WHERE a.specialist.id = :specialistProfileId AND a.startedAt BETWEEN :start AND :end AND a.status = :status")
    fun sumDurationMinutesBySpecialistAndStartedAtBetweenAndStatus(
        specialistProfileId: String,
        start: LocalDateTime,
        end: LocalDateTime,
        status: AttentionStatus
    ): Long

    fun findByClient_Id(clientId: String): List<Attention>
}
