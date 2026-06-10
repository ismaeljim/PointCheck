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

/**
 * Servicio encargado de gestionar las suscripciones de los profesionales.
 *
 * Controla el ciclo de vida de los planes de pago, incluyendo la creación,
 * verificación de vigencia y cancelación de suscripciones.
 */
@Service
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
    private val professionalProfileRepository: ProfessionalProfileRepository
) {

    /**
     * Registra una nueva suscripción para un perfil profesional.
     *
     * @param request Datos de la suscripción (ID de perfil, plan, fechas).
     * @return [SubscriptionResponse] con la suscripción creada.
     * @throws IllegalArgumentException si el perfil no existe o no está activo.
     */
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

    /**
     * Recupera la suscripción activa actual para un perfil profesional.
     *
     * Incluye lógica de validación de expiración: si la suscripción ha superado
     * su fecha de fin, se marca automáticamente como EXPIRED.
     *
     * @param professionalProfileId ID del perfil profesional.
     * @return [SubscriptionResponse] si existe una suscripción activa y vigente, null en caso contrario.
     */
    @Transactional
    fun getCurrentByProfessionalProfile(professionalProfileId: String): SubscriptionResponse? {
        val subscriptions = subscriptionRepository.findByProfessionalProfile_IdAndStatus(professionalProfileId, SubscriptionStatus.ACTIVE)
        
        if (subscriptions.isEmpty()) return null

        val current = subscriptions.first()
        
        if (current.endDate.isBefore(LocalDate.now())) {
            current.apply {
                status = SubscriptionStatus.EXPIRED
                updatedAt = LocalDateTime.now()
            }
            subscriptionRepository.save(current)
            return null
        }

        return current.toResponse()
    }

    /**
     * Cancela una suscripción activa de forma inmediata.
     *
     * @param id ID de la suscripción.
     * @return Suscripción actualizada con estado [SubscriptionStatus.CANCELLED].
     */
    @Transactional
    fun cancel(id: String): SubscriptionResponse {
        val subscription = subscriptionRepository.findById(id).orElseThrow {
            IllegalArgumentException("Suscripción no encontrada con ID: $id")
        }

        subscription.apply {
            status = SubscriptionStatus.CANCELLED
            updatedAt = LocalDateTime.now()
        }

        return subscriptionRepository.save(subscription).toResponse()
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
