package com.duoc.app.features.reservation.repository

import com.duoc.app.features.reservation.model.Reservation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ReservationRepository : JpaRepository<Reservation, Long> {
    fun findByClient_Id(clientId: Long): List<Reservation>
    fun findBySpecialist_Id(specialistId: Long): List<Reservation>

    fun findBySpecialist_IdAndReservationStartBetween(
        specialistId: Long,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<Reservation>

    fun findByClient_IdAndReservationStartAfter(
        clientId: Long,
        now: LocalDateTime
    ): List<Reservation>

    fun findBySpecialist_IdAndReservationStartAfter(
        specialistId: Long,
        now: LocalDateTime
    ): List<Reservation>
}
