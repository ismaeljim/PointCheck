package com.duoc.app.features.subscription.repository

import com.duoc.app.features.subscription.model.Subscription
import com.duoc.app.features.subscription.model.SubscriptionStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SubscriptionRepository : JpaRepository<Subscription, String> {
    fun findByProfessionalProfile_Id(professionalProfileId: String): List<Subscription>

    fun findByProfessionalProfile_IdAndStatus(
        professionalProfileId: String,
        status: SubscriptionStatus
    ): List<Subscription>
}
