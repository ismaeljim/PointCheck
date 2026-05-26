package com.duoc.app.features.admin.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "global_settings")
data class GlobalSettings(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "config_key", unique = true, nullable = false)
    val key: String,

    @Column(name = "config_value")
    var value: String,

    @Column(name = "description")
    val description: String? = null,

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
