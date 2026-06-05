package com.duoc.app.features.attention.repository

import com.duoc.app.features.attention.model.Attention
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface AttentionRepository : JpaRepository<Attention, String> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = ["client", "specialist", "reservation"])
    fun findByReservation_Id(reservationId: String): Attention?
    fun existsByReservation_Id(reservationId: String): Boolean

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = ["client", "specialist", "reservation"])
    fun findBySpecialist_IdAndStartedAtBetween(
        specialistId: String,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<Attention>

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = ["client", "specialist", "reservation"])
    fun findBySpecialist_IdAndStartedAtBetweenAndStatus(
        specialistId: String,
        start: LocalDateTime,
        end: LocalDateTime,
        status: com.duoc.app.features.attention.model.AttentionStatus
    ): List<Attention>

    fun countBySpecialist_IdAndStartedAtBetweenAndStatus(
        specialistId: String,
        start: LocalDateTime,
        end: LocalDateTime,
        status: com.duoc.app.features.attention.model.AttentionStatus
    ): Long

    @org.springframework.data.jpa.repository.Query("SELECT SUM(a.durationMinutes) FROM Attention a WHERE a.specialist.id = :specialistId AND a.startedAt BETWEEN :start AND :end AND a.status = :status")
    fun sumDurationMinutesBySpecialistAndStartedAtBetweenAndStatus(
        specialistId: String,
        start: LocalDateTime,
        end: LocalDateTime,
        status: com.duoc.app.features.attention.model.AttentionStatus
    ): Long?

    fun findByClient_Id(clientId: String): List<Attention>
    fun findBySpecialist_Id(specialistId: String?): List<Attention>
}
