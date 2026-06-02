package com.duoc.app.features.reservation.repository

import com.duoc.app.features.reservation.model.Reservation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ReservationRepository : JpaRepository<Reservation, String> {

    fun findByClient_Id(clientId: String): List<Reservation>

    fun findBySpecialist_Id(specialistId: String): List<Reservation>

    fun findBySpecialist_IdAndReservationStartBetween(
        specialistId: String?,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<Reservation>

    fun findByClient_IdAndReservationStartAfter(
        clientId: String,
        now: LocalDateTime
    ): List<Reservation>

    fun findBySpecialist_IdAndReservationStartAfter(
        specialistId: String,
        now: LocalDateTime
    ): List<Reservation>

    fun existsBySpecialist_IdAndReservationStartLessThanAndReservationEndGreaterThan(
        specialistId: String,
        end: LocalDateTime,
        start: LocalDateTime
    ): Boolean
}
