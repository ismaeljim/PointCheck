package com.duoc.app.features.admin.task

import com.duoc.app.features.admin.repository.AuditLogRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Tarea programada para la limpieza automática de registros de auditoría antiguos.
 * 
 * Mantiene la base de datos optimizada eliminando registros que superan los 30 días,
 * asegurando que solo el historial reciente esté disponible para consultas rápidas
 * mientras se asume que un sistema de auditoría externo o backups manejan el largo plazo.
 */
@Component
class AuditCleanupTask(
    private val auditLogRepository: AuditLogRepository
) {
    private val logger = LoggerFactory.getLogger(AuditCleanupTask::class.java)

    /**
     * Se ejecuta el primer día de cada mes a la medianoche.
     * Elimina todos los registros de auditoría con más de 30 días de antigüedad.
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional
    fun cleanupOldLogs() {
        val threshold = LocalDateTime.now().minusDays(30)
        logger.info("Iniciando limpieza de logs de auditoría anteriores a {}", threshold)
        
        try {
            val deletedCount = auditLogRepository.deleteByTimestampBefore(threshold)
            logger.info("Limpieza completada. Se eliminaron {} registros.", deletedCount)
        } catch (e: Exception) {
            logger.error("Error durante la limpieza de logs: {}", e.message)
        }
    }
}
