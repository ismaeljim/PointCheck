package com.duoc.app.features.subscription.repository

import com.duoc.app.features.subscription.model.Subscription
import com.duoc.app.features.subscription.model.SubscriptionStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SubscriptionRepository : JpaRepository<Subscription, Long> {
    fun findByProfessionalProfileId(professionalProfileId: Long): List<Subscription>

    fun findByProfessionalProfileIdAndStatus(
        professionalProfileId: Long,
        status: SubscriptionStatus
    ): List<Subscription>
}
