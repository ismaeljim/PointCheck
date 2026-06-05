package com.duoc.app.features.attention.service

import com.duoc.app.features.attention.dto.AttentionResponse
import com.duoc.app.features.attention.dto.FinishAttentionRequest
import com.duoc.app.features.attention.dto.StartAttentionRequest
import com.duoc.app.features.attention.model.Attention
import com.duoc.app.features.attention.model.AttentionStatus
import com.duoc.app.features.attention.repository.AttentionRepository
import com.duoc.app.features.reservation.model.ReservationStatus
import com.duoc.app.features.reservation.repository.ReservationRepository
import com.duoc.app.features.billing.dto.BillingRecordRequest
import com.duoc.app.features.billing.service.BillingService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * AUDITORÍA TÉCNICA: Flujo de Ejecución de Atenciones
 * 
 * Este servicio gestiona el ciclo de vida operativo de una cita desde que el especialista
 * inicia la sesión hasta que se genera el registro financiero.
 * 
 * Hallazgos de Implementación:
 * 1. [OK] Atomicidad: Uso de @Transactional para garantizar que el cambio de estado de la reserva, 
 *    la atención y la facturación ocurran como una única unidad de trabajo.
 * 2. [OK] Trazabilidad: Registro automático de 'startedAt' y 'finishedAt'.
 * 3. [OK] Integración de Cobro: Gatillado automático de 'billingService' al finalizar la atención.
 * 4. [BRECHA] Cálculo de Duración: Si no se provee 'durationMinutes', se calcula por diferencia de tiempo,
 *    lo cual es correcto para métricas de eficiencia del especialista.
 */
@Service
class AttentionService(
    private val attentionRepository: AttentionRepository,
    private val reservationRepository: ReservationRepository,
    private val billingService: BillingService
) {

    /**
     * AUDITORÍA: Inicio de Atención.
     * Cambia el estado de la reserva a CONFIRMED si estaba PENDING.
     * Evita duplicidad de atenciones para una misma reserva.
     */
    @Transactional
    fun start(request: StartAttentionRequest): AttentionResponse {
        val reservation = reservationRepository.findById(request.reservationId).orElseThrow {
            IllegalArgumentException("Reserva no encontrada con ID: ${request.reservationId}")
        }

        if (attentionRepository.existsByReservation_Id(request.reservationId)) {
            throw IllegalArgumentException("Ya existe una atención iniciada para esta reserva.")
        }

        val attention = Attention(
            reservation = reservation,
            client = reservation.client,
            specialist = reservation.specialist,
            startedAt = LocalDateTime.now(),
            status = AttentionStatus.IN_PROGRESS,
            observations = request.observations
        )

        if (reservation.status == ReservationStatus.PENDING) {
            val updatedReservation = reservation.copy(
                status = ReservationStatus.CONFIRMED,
                updatedAt = LocalDateTime.now()
            )
            reservationRepository.save(updatedReservation)
        }

        return attentionRepository.save(attention).toResponse()
    }

    /**
     * AUDITORÍA: Finalización y Facturación Automática.
     * 1. Marca la atención como FINISHED.
     * 2. Actualiza el estado de la reserva a COMPLETED.
     * 3. Crea el registro de facturación basado en el precio del servicio contratado.
     */
    @Transactional
    fun finish(attentionId: String, request: FinishAttentionRequest): AttentionResponse {
        val attention = attentionRepository.findById(attentionId).orElseThrow {
            IllegalArgumentException("Atención no encontrada con ID: $attentionId")
        }

        if (attention.status == AttentionStatus.FINISHED) {
            return attention.toResponse()
        }

        val finishedAt = LocalDateTime.now()
        val duration = request.durationMinutes ?: Duration.between(attention.startedAt, finishedAt).toMinutes().toInt()

        val updatedAttention = attention.copy(
            finishedAt = finishedAt,
            durationMinutes = duration,
            status = AttentionStatus.FINISHED,
            observations = request.observations ?: attention.observations,
            updatedAt = LocalDateTime.now()
        )

        val reservation = reservationRepository.findById(attention.reservation.id!!).orElseThrow {
            IllegalStateException("Reserva no encontrada para la atención: ${attention.id}")
        }
        
        val updatedReservation = reservation.copy(
            status = ReservationStatus.COMPLETED,
            updatedAt = LocalDateTime.now()
        )
        reservationRepository.save(updatedReservation)

        val savedAttention = attentionRepository.save(updatedAttention)

        // AUDITORÍA: El registro financiero nace de la oferta de servicio original.
        reservation.service?.let { service ->
            val price = service.price
            if (price != null) {
                billingService.create(
                    BillingRecordRequest(
                        reservationId = reservation.id!!,
                        attentionId = savedAttention.id,
                        amount = price,
                        notes = "Generado automáticamente al finalizar la atención"
                    )
                )
            }
        }

        return savedAttention.toResponse()
    }

    fun getTodayBySpecialist(specialistId: String): List<AttentionResponse> {
        val startOfDay = LocalDate.now().atStartOfDay()
        val endOfDay = startOfDay.plusDays(1)
        return attentionRepository.findBySpecialist_IdAndStartedAtBetween(specialistId, startOfDay, endOfDay)
            .map { it.toResponse() }
    }

    fun getHistoryByClient(clientId: String): List<AttentionResponse> {
        return attentionRepository.findByClient_Id(clientId).map { it.toResponse() }
    }

    private fun Attention.toResponse(): AttentionResponse = AttentionResponse(
        id = this.id!!,
        reservationId = this.reservation.id!!,
        client = this.client.toSummaryDto(),
        specialist = this.specialist.toSummaryDto(),
        startedAt = this.startedAt,
        finishedAt = this.finishedAt,
        durationMinutes = this.durationMinutes,
        status = this.status,
        observations = this.observations,
        createdAt = this.createdAt
    )
}
