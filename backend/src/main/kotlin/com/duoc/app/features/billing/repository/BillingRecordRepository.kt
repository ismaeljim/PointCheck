package com.duoc.app.features.billing.repository

import com.duoc.app.features.billing.model.BillingRecord
import com.duoc.app.features.billing.model.PaymentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface BillingRecordRepository : JpaRepository<BillingRecord, Long> {
    fun findByReservation_Id(reservationId: Long): List<BillingRecord>
    fun findByAttention_Id(attentionId: Long): List<BillingRecord>
    fun findBySpecialist_Id(specialistId: Long): List<BillingRecord>
    fun findByClient_Id(clientId: Long): List<BillingRecord>
    
    fun findBySpecialist_IdAndStatus(
        specialistId: Long,
        status: PaymentStatus
    ): List<BillingRecord>

    fun findBySpecialist_IdAndCreatedAtBetween(
        specialistId: Long,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<BillingRecord>

    fun findByStatus(status: PaymentStatus): List<BillingRecord>
}
