package com.duoc.app.features.billing.service

import com.duoc.app.features.attention.model.Attention
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

        var attention: Attention? = null
        if (request.attentionId != null) {
            attention = attentionRepository.findById(request.attentionId).orElseThrow {
                IllegalArgumentException("Atención no encontrada con ID: ${request.attentionId}")
            }
        }

        val billingRecord = BillingRecord(
            reservation = reservation,
            attention = attention,
            client = reservation.client,
            specialist = reservation.specialist,
            amount = request.amount,
            currency = request.currency,
            paymentMethod = request.paymentMethod,
            status = PaymentStatus.PENDING,
            notes = request.notes
        )

        return billingRecordRepository.save(billingRecord).toResponse()
    }

    @Transactional
    fun markAsPaid(id: String, request: MarkAsPaidRequest): BillingRecordResponse {
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
    fun cancel(id: String): BillingRecordResponse {
        val billingRecord = billingRecordRepository.findById(id).orElseThrow {
            IllegalArgumentException("Registro de cobro no encontrado con ID: $id")
        }

        val updatedRecord = billingRecord.copy(
            status = PaymentStatus.CANCELLED,
            updatedAt = LocalDateTime.now()
        )

        return billingRecordRepository.save(updatedRecord).toResponse()
    }

    fun getBySpecialist(specialistId: String): List<BillingRecordResponse> {
        return billingRecordRepository.findBySpecialist_Id(specialistId).map { it.toResponse() }
    }

    fun getPendingBySpecialist(specialistId: String): List<BillingRecordResponse> {
        return billingRecordRepository.findBySpecialist_IdAndStatus(specialistId, PaymentStatus.PENDING)
            .map { it.toResponse() }
    }

    fun getTodayBySpecialist(specialistId: String): List<BillingRecordResponse> {
        val startOfDay = LocalDate.now().atStartOfDay()
        val endOfDay = startOfDay.plusDays(1)
        return billingRecordRepository.findBySpecialist_IdAndCreatedAtBetween(specialistId, startOfDay, endOfDay)
            .map { it.toResponse() }
    }

    private fun BillingRecord.toResponse(): BillingRecordResponse = BillingRecordResponse(
        id = this.id,
        reservationId = this.reservation.id,
        attentionId = this.attention?.id,
        clientId = this.client.id,
        specialistId = this.specialist.id,
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
