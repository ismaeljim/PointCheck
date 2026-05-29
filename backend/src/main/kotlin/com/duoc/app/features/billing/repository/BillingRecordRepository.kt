package com.duoc.app.features.billing.repository

import com.duoc.app.features.billing.model.BillingRecord
import com.duoc.app.features.billing.model.PaymentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface BillingRecordRepository : JpaRepository<BillingRecord, String> {
    fun findByReservation_Id(reservationId: String): List<BillingRecord>
    fun findByAttention_Id(attentionId: String): List<BillingRecord>
    fun findBySpecialist_Id(specialistId: String): List<BillingRecord>
    fun findByClient_Id(clientId: String): List<BillingRecord>
    
    fun findBySpecialist_IdAndStatus(
        specialistId: String?,
        status: PaymentStatus
    ): List<BillingRecord>

    fun findBySpecialist_IdAndCreatedAtBetween(
        specialistId: String,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<BillingRecord>

    fun findByStatus(status: PaymentStatus): List<BillingRecord>
}
