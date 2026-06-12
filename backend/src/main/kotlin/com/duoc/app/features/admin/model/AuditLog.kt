package com.duoc.app.features.admin.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.Objects

/**
 * Entidad para el registro detallado de auditoría del sistema.
 * 
 * Basado en estándares de seguridad para trazabilidad, registra quién, qué, cuándo y dónde
 * se realizó una acción crítica dentro de la plataforma.
 */
@Entity
@Table(name = "audit_logs")
class AuditLog(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    val id: String? = null,

    @Column(nullable = false)
    var action: String, // e.g., "ACCESO", "CREAR", "EDITAR", "ELIMINAR"

    @Column(nullable = false)
    var performedByEmail: String,

    @Column(nullable = false)
    var performedByName: String,

    @Column(nullable = false)
    var targetType: String, // e.g., "Usuario", "Servicio", "Reserva", "Configuración"

    @Column(nullable = false)
    var targetId: String,

    @Column(nullable = true)
    var targetName: String? = null, // Nombre legible del objetivo (ej: Nombre del Cliente)

    @Column(length = 2000)
    var details: String? = null, // Descripción de cambios (ej: "Nombre: OLD -> NEW")

    @Column(nullable = true)
    var ipAddress: String? = null,

    @Column(nullable = false)
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AuditLog) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = Objects.hash(id)

    override fun toString(): String = "AuditLog(id=$id, action=$action, target=$targetType, timestamp=$timestamp)"
}
