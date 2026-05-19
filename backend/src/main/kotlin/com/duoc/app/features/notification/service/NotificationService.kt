package com.duoc.app.features.notification.service

import com.duoc.app.features.notification.model.Notification
import com.duoc.app.features.notification.model.NotificationType
import com.duoc.app.features.notification.repository.NotificationRepository
import com.duoc.app.features.user.model.User
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository
) {

    @Transactional
    fun createNotification(user: User, title: String, message: String, type: NotificationType = NotificationType.INFO): Notification {
        val notification = Notification(
            user = user,
            title = title,
            message = message,
            type = type
        )
        return notificationRepository.save(notification)
    }

    fun getRecentNotifications(userId: Long, limit: Int = 3): List<Notification> {
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit))
    }

    @Transactional
    fun markAsRead(notificationId: Long) {
        notificationRepository.findById(notificationId).ifPresent {
            it.isRead = true
            notificationRepository.save(it)
        }
    }
}
