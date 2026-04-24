package com.duoc.app.features.billing.repository

import com.duoc.app.features.billing.model.BillingRecord
import com.duoc.app.features.billing.model.PaymentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface BillingRecordRepository : JpaRepository<BillingRecord, Long> {
    fun findByReservationId(reservationId: Long): List<BillingRecord>
    fun findByAttentionId(attentionId: Long): List<BillingRecord>
    fun findBySpecialistId(specialistId: Long): List<BillingRecord>
    fun findByClientId(clientId: Long): List<BillingRecord>
    
    fun findBySpecialistIdAndStatus(
        specialistId: Long,
        status: PaymentStatus
    ): List<BillingRecord>

    fun findBySpecialistIdAndCreatedAtBetween(
        specialistId: Long,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<BillingRecord>
}
