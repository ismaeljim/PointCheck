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
 * Servicio encargado de gestionar el flujo de ejecución de las atenciones.
 *
 * Controla el ciclo de vida operativo de una cita desde que el especialista
 * inicia la sesión hasta que se genera el registro financiero. Garantiza la
 * atomicidad entre el estado de la reserva, el registro de atención y la
 * facturación.
 */
@Service
class AttentionService(
    private val attentionRepository: AttentionRepository,
    private val reservationRepository: ReservationRepository,
    private val billingService: BillingService
) {

    /**
     * Inicia formalmente una sesión de atención vinculada a una reserva.
     *
     * - Valida que no exista ya una atención en curso para esa reserva.
     * - Cambia el estado de la reserva a CONFIRMED.
     * - Registra la marca de tiempo de inicio.
     *
     * @param request Datos de inicio (ID de reserva y observaciones iniciales).
     * @return [AttentionResponse] con la atención en estado IN_PROGRESS.
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
            reservation.status = ReservationStatus.CONFIRMED
            reservation.updatedAt = LocalDateTime.now()
            reservationRepository.save(reservation)
        }

        return attentionRepository.save(attention).toResponse()
    }

    /**
     * Finaliza una sesión de atención y gatilla el proceso de facturación.
     *
     * 1. Marca la atención como FINISHED y calcula la duración real.
     * 2. Actualiza el estado de la reserva vinculada a COMPLETED.
     * 3. Crea automáticamente un registro de cobro (BillingRecord) basado en el precio del servicio.
     *
     * @param attentionId ID de la atención a finalizar.
     * @param request Datos de cierre (duración manual opcional y observaciones finales).
     * @return [AttentionResponse] con la atención finalizada.
     */
    @Transactional
    fun finish(attentionId: String, request: FinishAttentionRequest): AttentionResponse {
        val attention = attentionRepository.findById(attentionId).orElseThrow {
            IllegalArgumentException("Atención no encontrada con ID: $attentionId")
        }

        if (attention.status == AttentionStatus.FINISHED) {
            return attention.toResponse()
        }

        val reservation = attention.reservation

        // REGLA DE NEGOCIO: Restricción de Cobro Anticipado (CASH) con ventana de 60 min.
        val now = LocalDateTime.now()
        val limitForEarlyPayment = reservation.reservationStart.minusMinutes(60)
        
        if (reservation.paymentMethod == com.duoc.app.features.billing.model.PaymentMethod.CASH &&
            now.isBefore(limitForEarlyPayment)) {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
            throw IllegalStateException("Aún es temprano para cerrar esta atención. Por seguridad en cobros en efectivo, el sistema habilitará el cierre y cobro a partir de las ${limitForEarlyPayment.format(formatter)} hrs (60 minutos antes de la cita).")
        }

        val finishedAt = LocalDateTime.now()
        val duration = request.durationMinutes ?: Duration.between(attention.startedAt, finishedAt).toMinutes().toInt()

        attention.finishedAt = finishedAt
        attention.durationMinutes = duration
        attention.status = AttentionStatus.FINISHED
        attention.observations = request.observations ?: attention.observations
        attention.updatedAt = LocalDateTime.now()

        reservation.status = ReservationStatus.COMPLETED
        reservation.updatedAt = LocalDateTime.now()
        reservationRepository.save(reservation)

        val savedAttention = attentionRepository.save(attention)

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

    @Transactional(readOnly = true)
    fun getTodayBySpecialist(specialistProfileId: String): List<AttentionResponse> {
        val startOfDay = LocalDate.now().atStartOfDay()
        val endOfDay = startOfDay.plusDays(1)
        return attentionRepository.findBySpecialist_IdAndStartedAtBetween(specialistProfileId, startOfDay, endOfDay)
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getHistoryByClient(clientId: String): List<AttentionResponse> {
        return attentionRepository.findByClient_Id(clientId).map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getByReservationId(reservationId: String): AttentionResponse? {
        return attentionRepository.findByReservation_Id(reservationId)?.toResponse()
    }

    private fun Attention.toResponse(): AttentionResponse = AttentionResponse(
        id = this.id!!,
        reservationId = this.reservation.id!!,
        client = this.client.toSummaryDto(),
        specialist = this.specialist.toSummaryDto(),
        specialistProfileId = this.specialist.id ?: "",
        startedAt = this.startedAt,
        finishedAt = this.finishedAt,
        durationMinutes = this.durationMinutes,
        status = this.status,
        observations = this.observations,
        createdAt = this.createdAt
    )
}
