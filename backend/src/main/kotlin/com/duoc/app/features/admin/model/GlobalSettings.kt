package com.duoc.app.features.admin.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.Objects

@Entity
@Table(name = "global_settings")
class GlobalSettings(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    val id: String? = null,

    @Column(name = "config_key", unique = true, nullable = false)
    var key: String,

    @Column(name = "config_value")
    var value: String,

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GlobalSettings) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = Objects.hash(id)

    override fun toString(): String = "GlobalSettings(id=$id, key=$key, value=$value)"
}
