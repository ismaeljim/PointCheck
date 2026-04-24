package com.duoc.app.features.report.service

import com.duoc.app.features.attention.model.AttentionStatus
import com.duoc.app.features.attention.repository.AttentionRepository
import com.duoc.app.features.billing.model.PaymentStatus
import com.duoc.app.features.billing.repository.BillingRecordRepository
import com.duoc.app.features.report.dto.ReportSummaryResponse
import com.duoc.app.features.reservation.repository.ReservationRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
class ReportService(
    private val reservationRepository: ReservationRepository,
    private val attentionRepository: AttentionRepository,
    private val billingRecordRepository: BillingRecordRepository
) {

    fun getSummaryBySpecialist(specialistId: Long): ReportSummaryResponse {
        // Reservas
        val allReservations = reservationRepository.findBySpecialistId(specialistId)
        val today = LocalDate.now()
        val todayReservations = allReservations.filter { it.reservationStart.toLocalDate() == today }

        // Atenciones
        val allAttentions = attentionRepository.findBySpecialistId(specialistId)
        val finishedAttentions = allAttentions.filter { it.status == AttentionStatus.FINISHED }
        val avgMinutes = if (finishedAttentions.isNotEmpty()) {
            finishedAttentions.mapNotNull { it.durationMinutes }.average()
        } else {
            0.0
        }

        // Facturación
        val allBilling = billingRecordRepository.findBySpecialistId(specialistId)
        val paidBilling = allBilling.filter { it.status == PaymentStatus.PAID }
        val pendingBilling = allBilling.filter { it.status == PaymentStatus.PENDING }

        val totalCharged = paidBilling
            .map { it.amount }
            .fold(BigDecimal.ZERO, BigDecimal::add)

        val pendingAmount = pendingBilling
            .map { it.amount }
            .fold(BigDecimal.ZERO, BigDecimal::add)

        return ReportSummaryResponse(
            specialistId = specialistId,
            totalReservations = allReservations.size,
            todayReservations = todayReservations.size,
            completedAttentions = finishedAttentions.size,
            averageAttentionMinutes = avgMinutes,
            totalCharged = totalCharged,
            pendingAmount = pendingAmount,
            paidBillingCount = paidBilling.size,
            pendingBillingCount = pendingBilling.size
        )
    }
}
