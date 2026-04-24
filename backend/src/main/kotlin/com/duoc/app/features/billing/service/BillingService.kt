package com.duoc.app.features.billing.service

import com.duoc.app.features.attention.repository.AttentionRepository
import com.duoc.app.features.billing.dto.BillingRecordRequest
import com.duoc.app.features.billing.dto.BillingRecordResponse
import com.duoc.app.features.billing.dto.MarkAsPaidRequest
import com.duoc.app.features.billing.model.BillingRecord
import com.duoc.app.features.billing.model.PaymentStatus
import com.duoc.app.features.billing.repository.BillingRecordRepository
import com.duoc.app.features.reservation.repository.ReservationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class BillingService(
    private val billingRecordRepository: BillingRecordRepository,
    private val reservationRepository: ReservationRepository,
    private val attentionRepository: AttentionRepository
) {

    @Transactional
    fun create(request: BillingRecordRequest): BillingRecordResponse {
        val reservation = reservationRepository.findById(request.reservationId).orElseThrow {
            IllegalArgumentException("Reserva no encontrada con ID: ${request.reservationId}")
        }

        if (request.attentionId != null) {
            if (!attentionRepository.existsById(request.attentionId)) {
                throw IllegalArgumentException("Atención no encontrada con ID: ${request.attentionId}")
            }
        }

        val billingRecord = BillingRecord(
            reservationId = reservation.id,
            attentionId = request.attentionId,
            clientId = reservation.clientId,
            specialistId = reservation.specialistId,
            amount = request.amount,
            currency = request.currency,
            paymentMethod = request.paymentMethod,
            status = PaymentStatus.PENDING,
            notes = request.notes
        )

        return billingRecordRepository.save(billingRecord).toResponse()
    }

    @Transactional
    fun markAsPaid(id: Long, request: MarkAsPaidRequest): BillingRecordResponse {
        val billingRecord = billingRecordRepository.findById(id).orElseThrow {
            IllegalArgumentException("Registro de cobro no encontrado con ID: $id")
        }

        val updatedRecord = billingRecord.copy(
            status = PaymentStatus.PAID,
            paidAt = LocalDateTime.now(),
            paymentMethod = request.paymentMethod,
            externalReference = request.externalReference,
            notes = request.notes ?: billingRecord.notes,
            updatedAt = LocalDateTime.now()
        )

        return billingRecordRepository.save(updatedRecord).toResponse()
    }

    @Transactional
    fun cancel(id: Long): BillingRecordResponse {
        val billingRecord = billingRecordRepository.findById(id).orElseThrow {
            IllegalArgumentException("Registro de cobro no encontrado con ID: $id")
        }

        val updatedRecord = billingRecord.copy(
            status = PaymentStatus.CANCELLED,
            updatedAt = LocalDateTime.now()
        )

        return billingRecordRepository.save(updatedRecord).toResponse()
    }

    fun getBySpecialist(specialistId: Long): List<BillingRecordResponse> {
        return billingRecordRepository.findBySpecialistId(specialistId).map { it.toResponse() }
    }

    fun getPendingBySpecialist(specialistId: Long): List<BillingRecordResponse> {
        return billingRecordRepository.findBySpecialistIdAndStatus(specialistId, PaymentStatus.PENDING)
            .map { it.toResponse() }
    }

    fun getTodayBySpecialist(specialistId: Long): List<BillingRecordResponse> {
        val startOfDay = LocalDate.now().atStartOfDay()
        val endOfDay = startOfDay.plusDays(1)
        return billingRecordRepository.findBySpecialistIdAndCreatedAtBetween(specialistId, startOfDay, endOfDay)
            .map { it.toResponse() }
    }

    private fun BillingRecord.toResponse(): BillingRecordResponse = BillingRecordResponse(
        id = this.id,
        reservationId = this.reservationId,
        attentionId = this.attentionId,
        clientId = this.clientId,
        specialistId = this.specialistId,
        amount = this.amount,
        currency = this.currency,
        paymentMethod = this.paymentMethod,
        status = this.status,
        paidAt = this.paidAt,
        externalReference = this.externalReference,
        notes = this.notes,
        createdAt = this.createdAt
    )
}
