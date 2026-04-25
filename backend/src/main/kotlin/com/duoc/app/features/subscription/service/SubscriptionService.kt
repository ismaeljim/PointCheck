package com.duoc.app.features.subscription.service

import com.duoc.app.features.professionalprofile.repository.ProfessionalProfileRepository
import com.duoc.app.features.subscription.dto.SubscriptionRequest
import com.duoc.app.features.subscription.dto.SubscriptionResponse
import com.duoc.app.features.subscription.model.Subscription
import com.duoc.app.features.subscription.model.SubscriptionStatus
import com.duoc.app.features.subscription.repository.SubscriptionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
    private val professionalProfileRepository: ProfessionalProfileRepository
) {

    @Transactional
    fun create(request: SubscriptionRequest): SubscriptionResponse {
        val profile = professionalProfileRepository.findById(request.professionalProfileId).orElseThrow {
            IllegalArgumentException("Perfil profesional no encontrado con ID: ${request.professionalProfileId}")
        }

        if (!profile.active) {
            throw IllegalArgumentException("El perfil profesional con ID ${request.professionalProfileId} no está activo.")
        }

        val subscription = Subscription(
            professionalProfile = profile,
            planName = request.planName,
            startDate = request.startDate,
            endDate = request.endDate,
            status = SubscriptionStatus.ACTIVE
        )

        return subscriptionRepository.save(subscription).toResponse()
    }

    @Transactional
    fun getCurrentByProfessionalProfile(professionalProfileId: Long): SubscriptionResponse? {
        val subscriptions = subscriptionRepository.findByProfessionalProfile_IdAndStatus(professionalProfileId, SubscriptionStatus.ACTIVE)
        
        if (subscriptions.isEmpty()) return null

        val current = subscriptions.first()
        
        if (current.endDate.isBefore(LocalDate.now())) {
            val expiredSubscription = current.copy(
                status = SubscriptionStatus.EXPIRED,
                updatedAt = LocalDateTime.now()
            )
            subscriptionRepository.save(expiredSubscription)
            return null
        }

        return current.toResponse()
    }

    @Transactional
    fun cancel(id: Long): SubscriptionResponse {
        val subscription = subscriptionRepository.findById(id).orElseThrow {
            IllegalArgumentException("Suscripción no encontrada con ID: $id")
        }

        val updatedSubscription = subscription.copy(
            status = SubscriptionStatus.CANCELLED,
            updatedAt = LocalDateTime.now()
        )

        return subscriptionRepository.save(updatedSubscription).toResponse()
    }

    private fun Subscription.toResponse(): SubscriptionResponse = SubscriptionResponse(
        id = this.id,
        professionalProfileId = this.professionalProfile.id,
        planName = this.planName,
        status = this.status,
        startDate = this.startDate,
        endDate = this.endDate,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
