package com.duoc.app.features.admin.repository

import com.duoc.app.features.admin.model.AuditLog
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AuditLogRepository : JpaRepository<AuditLog, String> {
    fun findAllByOrderByTimestampDesc(pageable: Pageable): Page<AuditLog>
    fun countByTimestampBetween(start: java.time.LocalDateTime, end: java.time.LocalDateTime): Long
    fun deleteByTimestampBefore(threshold: java.time.LocalDateTime): Int
}
