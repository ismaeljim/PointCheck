package com.duoc.app.features.reservation.repository

import com.duoc.app.features.reservation.model.Reservation
import com.duoc.app.features.reservation.model.ReservationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.LocalDate

/**
 * Repositorio para la gestión de persistencia de Reservaciones.
 *
 * Implementa optimizaciones de rendimiento mediante @EntityGraph para evitar
 * el problema de consultas N+1 al cargar relaciones complejas (cliente, especialista,
 * servicios y perfiles profesionales).
 */
@Repository
interface ReservationRepository : JpaRepository<Reservation, String> {

    @EntityGraph(attributePaths = ["client", "specialist", "specialist.user", "specialist.category", "service"])
    fun findByClient_IdOrderByCreatedAtDesc(clientId: String): List<Reservation>

    @EntityGraph(attributePaths = ["client", "specialist", "specialist.user", "specialist.category", "service"])
    fun findBySpecialist_IdOrderByCreatedAtDesc(specialistProfileId: String): List<Reservation>

    @EntityGraph(attributePaths = ["client", "specialist", "specialist.user", "specialist.category", "service"])
    fun findBySpecialist_IdAndReservationStartBetween(specialistProfileId: String, start: LocalDateTime, end: LocalDateTime): List<Reservation>


    @EntityGraph(attributePaths = ["client", "specialist", "specialist.user", "specialist.category", "service"])
    fun findBySpecialist_IdAndReservationStartBetweenAndStatus(specialistProfileId: String, start: LocalDateTime, end: LocalDateTime, status: ReservationStatus): List<Reservation>

    @EntityGraph(attributePaths = ["client", "specialist", "specialist.user", "specialist.category", "service"])
    fun findByClient_IdAndReservationStartAfter(clientId: String, now: LocalDateTime): List<Reservation>

    @EntityGraph(attributePaths = ["client", "specialist", "specialist.user", "specialist.category", "service"])
    fun findByStatusAndReservationStartBefore(status: ReservationStatus, before: LocalDateTime): List<Reservation>

    fun findByReservationStartBetween(start: LocalDateTime, end: LocalDateTime): List<Reservation>

    @EntityGraph(attributePaths = ["client", "specialist", "specialist.user", "specialist.category", "service"])
    fun findBySpecialist_IdAndReservationStartBetweenAndService_Id(specialistProfileId: String, start: LocalDateTime, end: LocalDateTime, serviceId: String): List<Reservation>

    // Métodos de conteo optimizados para Dashboard
    fun countByReservationStartBetween(start: LocalDateTime, end: LocalDateTime): Long
    fun countBySpecialist_Id(specialistProfileId: String): Long
    fun countBySpecialist_IdAndReservationStartBetween(specialistProfileId: String, start: LocalDateTime, end: LocalDateTime): Long
    fun countBySpecialist_IdAndStatus(specialistProfileId: String, status: ReservationStatus): Long
    fun countBySpecialist_IdAndReservationStartBetweenAndStatus(specialistProfileId: String, start: LocalDateTime, end: LocalDateTime, status: ReservationStatus): Long

    @Query("SELECT COALESCE(SUM(r.service.durationMinutes), 0) FROM Reservation r WHERE r.specialist.id = :specialistProfileId AND r.reservationStart BETWEEN :start AND :end AND r.status = :status")
    fun sumServiceDurationMinutesBySpecialistAndReservationStartBetweenAndStatus(specialistProfileId: String, start: LocalDateTime, end: LocalDateTime, status: ReservationStatus): Long

    @Query("SELECT r FROM Reservation r WHERE r.specialist.id = :specialistId AND FUNCTION('DATE', r.reservationStart) = :date AND r.status <> 'CANCELLED'")
    fun findBySpecialistIdAndDate(
        @Param("specialistId") specialistId: String, 
        @Param("date") date: LocalDate
    ): List<Reservation>

    fun existsBySpecialist_IdAndReservationStartLessThanAndReservationEndGreaterThanAndStatusNot(
        specialistProfileId: String,
        end: LocalDateTime,
        start: LocalDateTime,
        status: ReservationStatus
    ): Boolean
}
