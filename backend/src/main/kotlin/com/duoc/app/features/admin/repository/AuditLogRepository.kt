package com.duoc.app.features.admin.repository

import com.duoc.app.features.admin.model.AuditLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AuditLogRepository : JpaRepository<AuditLog, String> {
    fun findAllByOrderByTimestampDesc(): List<AuditLog>
}
