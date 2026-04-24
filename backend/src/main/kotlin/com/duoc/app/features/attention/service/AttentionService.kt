package com.duoc.app.features.attention.service

import com.duoc.app.features.attention.dto.AttentionResponse
import com.duoc.app.features.attention.dto.FinishAttentionRequest
import com.duoc.app.features.attention.dto.StartAttentionRequest
import com.duoc.app.features.attention.model.Attention
import com.duoc.app.features.attention.model.AttentionStatus
import com.duoc.app.features.attention.repository.AttentionRepository
import com.duoc.app.features.reservation.model.ReservationStatus
import com.duoc.app.features.reservation.repository.ReservationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class AttentionService(
    private val attentionRepository: AttentionRepository,
    private val reservationRepository: ReservationRepository
) {

    @Transactional
    fun start(request: StartAttentionRequest): AttentionResponse {
        val reservation = reservationRepository.findById(request.reservationId).orElseThrow {
            IllegalArgumentException("Reserva no encontrada con ID: ${request.reservationId}")
        }

        if (attentionRepository.existsByReservationId(request.reservationId)) {
            throw IllegalArgumentException("Ya existe una atención iniciada para esta reserva.")
        }

        val attention = Attention(
            reservationId = reservation.id,
            clientId = reservation.clientId,
            specialistId = reservation.specialistId,
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

    @Transactional
    fun finish(attentionId: Long, request: FinishAttentionRequest): AttentionResponse {
        val attention = attentionRepository.findById(attentionId).orElseThrow {
            IllegalArgumentException("Atención no encontrada con ID: $attentionId")
        }

        if (attention.status == AttentionStatus.FINISHED) {
            return attention.toResponse()
        }

        val finishedAt = LocalDateTime.now()
        val duration = Duration.between(attention.startedAt, finishedAt).toMinutes().toInt()

        val updatedAttention = attention.copy(
            finishedAt = finishedAt,
            durationMinutes = duration,
            status = AttentionStatus.FINISHED,
            observations = request.observations ?: attention.observations,
            updatedAt = LocalDateTime.now()
        )

        val reservation = reservationRepository.findById(attention.reservationId).orElseThrow {
            IllegalStateException("Reserva no encontrada para la atención: ${attention.id}")
        }
        
        val updatedReservation = reservation.copy(
            status = ReservationStatus.COMPLETED,
            updatedAt = LocalDateTime.now()
        )
        reservationRepository.save(updatedReservation)

        return attentionRepository.save(updatedAttention).toResponse()
    }

    fun getTodayBySpecialist(specialistId: Long): List<AttentionResponse> {
        val startOfDay = LocalDate.now().atStartOfDay()
        val endOfDay = startOfDay.plusDays(1)
        return attentionRepository.findBySpecialistIdAndStartedAtBetween(specialistId, startOfDay, endOfDay)
            .map { it.toResponse() }
    }

    fun getHistoryByClient(clientId: Long): List<AttentionResponse> {
        return attentionRepository.findByClientId(clientId).map { it.toResponse() }
    }

    private fun Attention.toResponse(): AttentionResponse = AttentionResponse(
        id = this.id,
        reservationId = this.reservationId,
        clientId = this.clientId,
        specialistId = this.specialistId,
        startedAt = this.startedAt,
        finishedAt = this.finishedAt,
        durationMinutes = this.durationMinutes,
        status = this.status,
        observations = this.observations,
        createdAt = this.createdAt
    )
}
