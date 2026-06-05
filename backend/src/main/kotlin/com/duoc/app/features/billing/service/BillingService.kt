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

/**
 * AUDITORÍA TÉCNICA: Gestión de Facturación y Cobranza
 * 
 * Este servicio centraliza el control financiero de la plataforma, permitiendo
 * rastrear los montos adeudados y pagados por los servicios prestados.
 * 
 * Hallazgos de Implementación:
 * 1. [OK] Relación de Trazabilidad: Cada registro de cobro se vincula a una 'reservation' y opcionalmente a una 'attention'.
 * 2. [OK] Multi-Moneda: Soporte base para 'currency' (Default: CLP).
 * 3. [MEJORA] Integración de Pasarela: Actualmente solo gestiona estados lógicos (PENDING/PAID).
 *    Falta integración con Webpay/Stripe para cobros digitales reales.
 * 4. [OK] Referencia Externa: El campo 'externalReference' permite conciliar con transferencias o comprobantes físicos.
 */
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
            currency = request.currency ?: "CLP",
            paymentMethod = request.paymentMethod ?: reservation.paymentMethod,
            status = PaymentStatus.PENDING,
            notes = request.notes
        )

        return billingRecordRepository.save(billingRecord).toResponse()
    }

    /**
     * AUDITORÍA: Cambio de Estado Financiero.
     * Esencial para la liquidación de haberes del especialista.
     */
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
        id = this.id!!,
        reservationId = this.reservation.id!!,
        attentionId = this.attention?.id,
        client = this.client.toSummaryDto(),
        specialist = this.specialist.toSummaryDto(),
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
