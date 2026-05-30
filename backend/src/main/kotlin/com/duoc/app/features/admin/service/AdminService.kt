package com.duoc.app.features.admin.service

import com.duoc.app.features.admin.model.AuditLog
import com.duoc.app.features.admin.model.GlobalSettings
import com.duoc.app.features.admin.repository.AuditLogRepository
import com.duoc.app.features.admin.repository.GlobalSettingsRepository
import com.duoc.app.features.billing.model.PaymentStatus
import com.duoc.app.features.billing.repository.BillingRecordRepository
import com.duoc.app.features.user.model.User
import com.duoc.app.features.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AdminService(
    private val userRepository: UserRepository,
    private val billingRecordRepository: BillingRecordRepository,
    private val settingsRepository: GlobalSettingsRepository,
    private val auditLogRepository: AuditLogRepository
) {

    fun getAllUsers(): List<User> = userRepository.findAll()

    @Transactional
    fun toggleUserStatus(userId: String, adminEmail: String): User {
        val user = userRepository.findById(userId).orElseThrow { RuntimeException("User not found") }
        user.active = !user.active
        val savedUser = userRepository.save(user)
        
        auditLogRepository.save(AuditLog(
            action = if (savedUser.active) "ACTIVATE_USER" else "DEACTIVATE_USER",
            performedBy = adminEmail,
            targetType = "USER",
            targetId = userId,
            details = "User ${user.email} status toggled to ${savedUser.active}"
        ))
        
        return savedUser
    }

    fun getFinancialReport(): Map<String, Any> {
        val billing = billingRecordRepository.findAll()
        val totalRevenue = billing.filter { it.status == PaymentStatus.PAID }.sumOf { it.amount.toDouble() }
        val pendingRevenue = billing.filter { it.status == PaymentStatus.PENDING }.sumOf { it.amount.toDouble() }
        
        return mapOf(
            "totalRevenue" to totalRevenue,
            "pendingRevenue" to pendingRevenue,
            "totalTransactions" to billing.size,
            "paidTransactions" to billing.count { it.status == PaymentStatus.PAID },
            "reportDate" to LocalDateTime.now()
        )
    }

    fun getSettings(): List<GlobalSettings> = settingsRepository.findAll()

    @Transactional
    fun updateSetting(key: String, value: String, adminEmail: String): GlobalSettings {
        val setting = settingsRepository.findByKey(key).orElseGet {
            GlobalSettings(key = key, value = value, description = "Auto-generated setting")
        }
        val oldValue = setting.value
        setting.value = value
        setting.updatedAt = LocalDateTime.now()
        val saved = settingsRepository.save(setting)

        auditLogRepository.save(AuditLog(
            action = "UPDATE_SETTING",
            performedBy = adminEmail,
            targetType = "SETTING",
            targetId = key,
            details = "Changed '$key' from '$oldValue' to '$value'"
        ))

        return saved
    }

    fun getAuditLogs(): List<AuditLog> = auditLogRepository.findAllByOrderByTimestampDesc()
}
