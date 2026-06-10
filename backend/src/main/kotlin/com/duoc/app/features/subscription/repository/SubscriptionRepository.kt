package com.duoc.app.features.subscription.repository

import com.duoc.app.features.subscription.model.Subscription
import com.duoc.app.features.subscription.model.SubscriptionStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repositorio para la gestión de Suscripciones.
 *
 * Proporciona métodos para consultar el estado y el historial de planes
 * contratados por los profesionales.
 */
@Repository
interface SubscriptionRepository : JpaRepository<Subscription, String> {
    fun findByProfessionalProfile_Id(professionalProfileId: String): List<Subscription>

    fun findByProfessionalProfile_IdAndStatus(
        professionalProfileId: String,
        status: SubscriptionStatus
    ): List<Subscription>
}
