package com.duoc.app.features.notification.repository

import com.duoc.app.features.notification.model.Notification
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationRepository : JpaRepository<Notification, Long> {
    fun findByUser_IdOrderByCreatedAtDesc(userId: Long, pageable: Pageable): List<Notification>
    fun countByUser_IdAndIsReadFalse(userId: Long): Long
}
