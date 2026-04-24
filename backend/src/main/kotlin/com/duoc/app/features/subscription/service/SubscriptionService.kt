package com.duoc.app.features.subscription.service

import com.duoc.app.features.subscription.dto.SubscriptionRequest
import com.duoc.app.features.subscription.dto.SubscriptionResponse
import com.duoc.app.features.subscription.model.Subscription
import com.duoc.app.features.subscription.model.SubscriptionStatus
import com.duoc.app.features.subscription.repository.SubscriptionRepository
import com.duoc.app.features.user.model.UserRole
import com.duoc.app.features.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun create(request: SubscriptionRequest): SubscriptionResponse {
        val user = userRepository.findById(request.specialistId).orElseThrow {
            IllegalArgumentException("Usuario no encontrado con ID: ${request.specialistId}")
        }

        if (user.role != UserRole.SPECIALIST) {
            throw IllegalArgumentException("El usuario con ID ${request.specialistId} no es un especialista.")
        }

        val subscription = Subscription(
            specialistId = request.specialistId,
            planName = request.planName,
            startDate = request.startDate,
            endDate = request.endDate,
            status = SubscriptionStatus.ACTIVE
        )

        return subscriptionRepository.save(subscription).toResponse()
    }

    @Transactional
    fun getCurrentBySpecialist(specialistId: Long): SubscriptionResponse? {
        val subscriptions = subscriptionRepository.findBySpecialistIdAndStatus(specialistId, SubscriptionStatus.ACTIVE)
        
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
        specialistId = this.specialistId,
        planName = this.planName,
        status = this.status,
        startDate = this.startDate,
        endDate = this.endDate,
        createdAt = this.createdAt
    )
}
