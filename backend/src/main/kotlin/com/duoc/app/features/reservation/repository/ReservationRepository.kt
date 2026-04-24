package com.duoc.app.features.reservation.repository

import com.duoc.app.features.reservation.model.Reservation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ReservationRepository : JpaRepository<Reservation, Long> {
    fun findByClientId(clientId: Long): List<Reservation>
    fun findBySpecialistId(specialistId: Long): List<Reservation>

    fun findBySpecialistIdAndReservationStartBetween(
        specialistId: Long,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<Reservation>

    fun findByClientIdAndReservationStartAfter(
        clientId: Long,
        now: LocalDateTime
    ): List<Reservation>

    fun findBySpecialistIdAndReservationStartAfter(
        specialistId: Long,
        now: LocalDateTime
    ): List<Reservation>
}
