package com.duoc.app.features.admin.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.Objects

@Entity
@Table(name = "audit_logs")
class AuditLog(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    val id: String? = null,

    @Column(nullable = false)
    var action: String,

    @Column(nullable = false)
    var performedBy: String, // Email or Name of the admin

    @Column(nullable = false)
    var targetType: String, // e.g., "USER", "SETTING", "BILLING"

    @Column(nullable = false)
    var targetId: String,

    @Column(length = 1000)
    var details: String? = null,

    @Column(nullable = false)
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AuditLog) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = Objects.hash(id)

    override fun toString(): String = "AuditLog(id=$id, action=$action, timestamp=$timestamp)"
}
