package com.duoc.app.features.reservation.repository

import com.duoc.app.features.reservation.model.Reservation
import com.duoc.app.features.reservation.model.ReservationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Repositorio para la gestión de persistencia de Reservaciones.
 *
 * Implementa optimizaciones de rendimiento mediante @EntityGraph para evitar
 * el problema de consultas N+1 al cargar relaciones complejas (cliente, especialista,
 * servicios y perfiles profesionales).
 */
@Repository
interface ReservationRepository : JpaRepository<Reservation, String> {

    @EntityGraph(attributePaths = ["client", "specialist", "service", "service.professionalProfile", "service.professionalProfile.category"])
    fun findByClient_Id(clientId: String): List<Reservation>

    @EntityGraph(attributePaths = ["client", "specialist", "service", "service.professionalProfile", "service.professionalProfile.category"])
    fun findBySpecialist_Id(specialistId: String): List<Reservation>

    @EntityGraph(attributePaths = ["client", "specialist", "service", "service.professionalProfile", "service.professionalProfile.category"])
    fun findBySpecialist_IdAndReservationStartBetween(specialistId: String?, start: LocalDateTime, end: LocalDateTime): List<Reservation>


    @EntityGraph(attributePaths = ["client", "specialist", "service", "service.professionalProfile", "service.professionalProfile.category"])
    fun findBySpecialist_IdAndReservationStartBetweenAndStatus(specialistId: String, start: LocalDateTime, end: LocalDateTime, status: ReservationStatus): List<Reservation>

    @EntityGraph(attributePaths = ["client", "specialist", "service", "service.professionalProfile", "service.professionalProfile.category"])
    fun findByClient_IdAndReservationStartAfter(clientId: String, now: LocalDateTime): List<Reservation>

    @EntityGraph(attributePaths = ["client", "specialist", "service", "service.professionalProfile"])
    fun findBySpecialist_IdAndReservationStartBetweenAndService_Id(specialistId: String, start: LocalDateTime, end: LocalDateTime, serviceId: String): List<Reservation>

    // Métodos de conteo optimizados para Dashboard
    fun countBySpecialist_Id(specialistId: String): Long
    fun countBySpecialist_IdAndReservationStartBetween(specialistId: String, start: LocalDateTime, end: LocalDateTime): Long
    fun countBySpecialist_IdAndStatus(specialistId: String, status: ReservationStatus): Long
    fun countBySpecialist_IdAndReservationStartBetweenAndStatus(specialistId: String, start: LocalDateTime, end: LocalDateTime, status: ReservationStatus): Long

    @Query("SELECT SUM(r.service.durationMinutes) FROM Reservation r WHERE r.specialist.id = :specialistId AND r.reservationStart BETWEEN :start AND :end AND r.status = :status")
    fun sumServiceDurationMinutesBySpecialistAndReservationStartBetweenAndStatus(specialistId: String, start: LocalDateTime, end: LocalDateTime, status: ReservationStatus): Long?

    fun existsBySpecialist_IdAndReservationStartLessThanAndReservationEndGreaterThanAndStatusNot(
        specialistId: String,
        end: LocalDateTime,
        start: LocalDateTime,
        status: ReservationStatus
    ): Boolean
}
