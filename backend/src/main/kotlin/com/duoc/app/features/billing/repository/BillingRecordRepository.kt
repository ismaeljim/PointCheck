package com.duoc.app.features.billing.repository

import com.duoc.app.features.billing.model.BillingRecord
import com.duoc.app.features.billing.model.PaymentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface BillingRecordRepository : JpaRepository<BillingRecord, String> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = ["reservation", "attention", "reservation.client", "reservation.specialist"])
    fun findByReservation_Id(reservationId: String): List<BillingRecord>
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = ["reservation", "attention", "reservation.client", "reservation.specialist"])
    fun findByAttention_Id(attentionId: String): List<BillingRecord>
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = ["reservation", "attention", "reservation.client", "reservation.specialist"])
    fun findByReservation_Specialist_Id(specialistProfileId: String): List<BillingRecord>
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = ["reservation", "attention", "reservation.client", "reservation.specialist"])
    fun findByReservation_Client_Id(clientId: String): List<BillingRecord>
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = ["reservation", "attention", "reservation.client", "reservation.specialist"])
    fun findByReservation_Specialist_IdAndStatus(
        specialistProfileId: String?,
        status: PaymentStatus
    ): List<BillingRecord>

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = ["reservation", "attention", "reservation.client", "reservation.specialist"])
    fun findByReservation_Specialist_IdAndCreatedAtBetween(
        specialistProfileId: String,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<BillingRecord>

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(b.amount), 0) FROM BillingRecord b WHERE b.reservation.specialist.id = :specialistProfileId AND b.createdAt BETWEEN :start AND :end AND b.status = :status")
    fun sumAmountBySpecialistAndCreatedAtBetweenAndStatus(
        specialistProfileId: String,
        start: java.time.LocalDateTime,
        end: java.time.LocalDateTime,
        status: PaymentStatus
    ): java.math.BigDecimal

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(b.amount), 0) FROM BillingRecord b WHERE b.reservation.specialist.id = :specialistProfileId AND b.reservation.reservationStart BETWEEN :start AND :end AND b.status = :status")
    fun sumAmountBySpecialistAndReservationDateBetweenAndStatus(
        specialistProfileId: String,
        start: java.time.LocalDateTime,
        end: java.time.LocalDateTime,
        status: PaymentStatus
    ): java.math.BigDecimal

    fun findByReservation_Specialist_IdAndCreatedAtBetweenAndStatus(
        specialistProfileId: String,
        start: LocalDateTime,
        end: LocalDateTime,
        status: PaymentStatus
    ): List<BillingRecord>

    fun countByReservation_Specialist_IdAndCreatedAtBetweenAndStatus(
        specialistProfileId: String,
        start: LocalDateTime,
        end: LocalDateTime,
        status: PaymentStatus
    ): Long

    fun findByReservation_Specialist_IdAndCreatedAtBetweenAndReservation_Service_Id(
        specialistProfileId: String,
        start: LocalDateTime,
        end: LocalDateTime,
        serviceId: String
    ): List<BillingRecord>

    fun findByReservation_Specialist_IdAndCreatedAtBetweenAndReservation_Service_IdAndStatus(
        specialistProfileId: String,
        start: LocalDateTime,
        end: LocalDateTime,
        serviceId: String,
        status: PaymentStatus
    ): List<BillingRecord>

    fun findByStatus(status: PaymentStatus): List<BillingRecord>

    fun findByCreatedAtBetween(start: LocalDateTime, end: LocalDateTime): List<BillingRecord>
}
