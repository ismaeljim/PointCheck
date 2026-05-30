package com.duoc.app.features.notification.model

import com.duoc.app.features.user.model.User
import jakarta.persistence.*
import java.time.LocalDateTime

enum class NotificationType {
    INFO, CONFIRMATION, ALERT
}

@Entity
@Table(name = "notifications")
class Notification(
    @Id
    val id: String = java.util.UUID.randomUUID().toString(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false, length = 1000)
    val message: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: NotificationType = NotificationType.INFO,

    @Column(nullable = false)
    var isRead: Boolean = false,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
