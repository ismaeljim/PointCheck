package com.duoc.app.core.audit

import com.duoc.app.features.admin.model.AuditLog
import com.duoc.app.features.admin.repository.AuditLogRepository
import com.duoc.app.features.user.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

/**
 * Componente centralizado para el registro de auditoría.
 * 
 * Permite registrar acciones de forma consistente a través de toda la aplicación.
 * Utiliza propagación REQUIRES_NEW para asegurar que el log se guarde incluso si
 * la transacción principal falla (útil para registrar intentos fallidos).
 */
@Component
class AuditLogger(
    private val auditLogRepository: AuditLogRepository,
    private val userRepository: UserRepository
) {

    /**
     * Registra una acción en el historial de auditoría.
     * Automáticamente intenta capturar el usuario autenticado y la dirección IP.
     * 
     * @param action Acción realizada (ACCESO, CREAR, EDITAR, ELIMINAR, etc.)
     * @param targetType Tipo de entidad afectada (Usuario, Servicio, Reserva, etc.)
     * @param targetId ID de la entidad afectada.
     * @param targetName Nombre legible de la entidad (opcional).
     * @param details Descripción de los cambios o información adicional (opcional).
     * @param performedByEmail Email explícito (opcional, si no hay contexto de seguridad).
     * @param performedByName Nombre explícito (opcional, si no hay contexto de seguridad).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun log(
        action: String,
        targetType: String,
        targetId: String,
        targetName: String? = null,
        details: String? = null,
        performedByEmail: String? = null,
        performedByName: String? = null
    ) {
        val auth = SecurityContextHolder.getContext().authentication
        var finalEmail = performedByEmail
        var finalName = performedByName

        if (auth != null && auth.isAuthenticated && auth.name != "anonymousUser") {
            finalEmail = auth.name
            if (finalName == null) {
                finalName = userRepository.findByEmail(auth.name)?.name ?: "Usuario Autenticado"
            }
        }

        val request = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request
        val ipAddress = request?.let { getClientIp(it) }

        val auditLog = AuditLog(
            action = action,
            performedByEmail = finalEmail ?: "SYSTEM",
            performedByName = finalName ?: "Sistema",
            targetType = targetType,
            targetId = targetId,
            targetName = targetName,
            details = details,
            ipAddress = ipAddress
        )
        auditLogRepository.save(auditLog)
    }

    private fun getClientIp(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        return if (!xForwardedFor.isNullOrEmpty()) {
            xForwardedFor.split(",")[0]
        } else {
            request.remoteAddr
        }
    }
}
