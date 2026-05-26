package com.duoc.app.features.admin.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "audit_logs")
data class AuditLog(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val action: String,

    @Column(nullable = false)
    val performedBy: String, // Email or Name of the admin

    @Column(nullable = false)
    val targetType: String, // e.g., "USER", "SETTING", "BILLING"

    @Column(nullable = false)
    val targetId: String,

    @Column(length = 1000)
    val details: String? = null,

    @Column(nullable = false)
    val timestamp: LocalDateTime = LocalDateTime.now()
)
