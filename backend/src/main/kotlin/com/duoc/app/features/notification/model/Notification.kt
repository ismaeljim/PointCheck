package com.duoc.app.features.notification.model

import com.duoc.app.features.user.model.User
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.Objects

enum class NotificationType {
    INFO, CONFIRMATION, ALERT
}

@Entity
@Table(name = "notifications")
class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    val id: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false, length = 1000)
    var message: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: NotificationType = NotificationType.INFO,

    @Column(nullable = false)
    var isRead: Boolean = false,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Notification) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = Objects.hash(id)

    override fun toString(): String = "Notification(id=$id, title=$title, isRead=$isRead)"
}
