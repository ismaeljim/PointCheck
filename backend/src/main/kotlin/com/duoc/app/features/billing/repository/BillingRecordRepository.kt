package com.duoc.app.features.billing.repository

import com.duoc.app.features.billing.model.BillingRecord
import com.duoc.app.features.billing.model.PaymentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface BillingRecordRepository : JpaRepository<BillingRecord, String> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = ["client", "specialist", "reservation", "attention"])
    fun findByReservation_Id(reservationId: String): List<BillingRecord>
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = ["client", "specialist", "reservation", "attention"])
    fun findByAttention_Id(attentionId: String): List<BillingRecord>
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = ["client", "specialist", "reservation", "attention"])
    fun findBySpecialist_Id(specialistId: String): List<BillingRecord>
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = ["client", "specialist", "reservation", "attention"])
    fun findByClient_Id(clientId: String): List<BillingRecord>
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = ["client", "specialist", "reservation", "attention"])
    fun findBySpecialist_IdAndStatus(
        specialistId: String?,
        status: PaymentStatus
    ): List<BillingRecord>

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = ["client", "specialist", "reservation", "attention"])
    fun findBySpecialist_IdAndCreatedAtBetween(
        specialistId: String,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<BillingRecord>

    @org.springframework.data.jpa.repository.Query("SELECT SUM(b.amount) FROM BillingRecord b WHERE b.specialist.id = :specialistId AND b.createdAt BETWEEN :start AND :end AND b.status = :status")
    fun sumAmountBySpecialistAndCreatedAtBetweenAndStatus(
        specialistId: String,
        start: LocalDateTime,
        end: LocalDateTime,
        status: PaymentStatus
    ): java.math.BigDecimal?

    fun findBySpecialist_IdAndCreatedAtBetweenAndStatus(
        specialistId: String,
        start: LocalDateTime,
        end: LocalDateTime,
        status: PaymentStatus
    ): List<BillingRecord>

    fun countBySpecialist_IdAndCreatedAtBetweenAndStatus(
        specialistId: String,
        start: LocalDateTime,
        end: LocalDateTime,
        status: PaymentStatus
    ): Long

    fun findBySpecialist_IdAndCreatedAtBetweenAndReservation_Service_Id(
        specialistId: String,
        start: LocalDateTime,
        end: LocalDateTime,
        serviceId: String
    ): List<BillingRecord>

    fun findBySpecialist_IdAndCreatedAtBetweenAndReservation_Service_IdAndStatus(
        specialistId: String,
        start: LocalDateTime,
        end: LocalDateTime,
        serviceId: String,
        status: PaymentStatus
    ): List<BillingRecord>

    fun findByStatus(status: PaymentStatus): List<BillingRecord>
}
