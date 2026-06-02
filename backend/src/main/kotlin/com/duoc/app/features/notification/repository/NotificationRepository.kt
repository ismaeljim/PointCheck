package com.duoc.app.features.notification.repository

import com.duoc.app.features.notification.model.Notification
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface NotificationRepository : JpaRepository<Notification, String> {
    
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId ORDER BY n.createdAt DESC")
    fun findByUserIdOrderByCreatedAtDesc(@Param("userId") userId: String, pageable: Pageable): List<Notification>

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false")
    fun countUnreadByUserId(@Param("userId") userId: String): Long
}
