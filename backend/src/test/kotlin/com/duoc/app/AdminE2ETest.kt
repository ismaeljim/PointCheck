package com.duoc.app

import com.duoc.app.features.admin.model.AuditLog
import com.duoc.app.features.admin.model.GlobalSettings
import com.duoc.app.features.admin.repository.AuditLogRepository
import com.duoc.app.features.admin.repository.GlobalSettingsRepository
import com.duoc.app.features.attention.repository.AttentionRepository
import com.duoc.app.features.billing.repository.BillingRecordRepository
import com.duoc.app.features.professionalprofile.repository.ProfessionalProfileRepository
import com.duoc.app.features.reservation.repository.ReservationRepository
import com.duoc.app.features.service.repository.CategoryRepository
import com.duoc.app.features.service.repository.ServiceOfferingRepository
import com.duoc.app.features.user.model.User
import com.duoc.app.features.user.model.UserRole
import com.duoc.app.features.user.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.patch
import org.springframework.transaction.annotation.Transactional
import java.security.Principal

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AdminE2ETest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var settingsRepository: GlobalSettingsRepository

    @Autowired
    private lateinit var auditLogRepository: AuditLogRepository

    @Autowired
    private lateinit var billingRecordRepository: BillingRecordRepository

    @Autowired
    private lateinit var attentionRepository: AttentionRepository

    @Autowired
    private lateinit var reservationRepository: ReservationRepository

    @Autowired
    private lateinit var serviceOfferingRepository: ServiceOfferingRepository

    @Autowired
    private lateinit var professionalProfileRepository: ProfessionalProfileRepository

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    private lateinit var testUser: User

    @BeforeEach
    fun setup() {
        // Delete in reverse order of dependencies to avoid FK violations
        billingRecordRepository.deleteAll()
        attentionRepository.deleteAll()
        reservationRepository.deleteAll()
        serviceOfferingRepository.deleteAll()
        professionalProfileRepository.deleteAll()
        categoryRepository.deleteAll()
        auditLogRepository.deleteAll()
        settingsRepository.deleteAll()
        userRepository.deleteAll()

        testUser = userRepository.save(
            User(
                email = "user@test.com",
                name = "Test User",
                password = "password",
                rut = "12345678-9",
                phone = "+56912345678",
                role = UserRole.CLIENT,
                active = true
            )
        )

        settingsRepository.save(GlobalSettings(key = "TEST_SETTING", value = "OLD_VALUE"))
    }

    private class MockPrincipal(private val name: String) : Principal {
        override fun getName(): String = name
    }

    @Test
    fun `toggle user status should create audit log`() {
        mockMvc.patch("/api/admin/users/${testUser.id}/toggle-status") {
            principal = MockPrincipal("admin@pointcheck.cl")
        }.andExpect { status { isOk() } }

        val logs = auditLogRepository.findAll()
        assertTrue(logs.any { (it.action == "ACTIVAR" || it.action == "DESACTIVAR") && it.targetId == testUser.id.toString() })
    }

    @Test
    fun `update setting should create audit log`() {
        mockMvc.post("/api/admin/settings") {
            param("key", "TEST_SETTING")
            param("value", "NEW_VALUE")
            principal = MockPrincipal("admin@pointcheck.cl")
        }.andExpect { status { isOk() } }

        val logs = auditLogRepository.findAll()
        assertTrue(logs.any { it.action == "EDITAR" && it.details?.contains("NEW_VALUE") == true })
        
        val setting = settingsRepository.findByKey("TEST_SETTING").get()
        assertEquals("NEW_VALUE", setting.value)
    }

    @Test
    fun `get audit logs should return created logs`() {
        mockMvc.patch("/api/admin/users/${testUser.id}/toggle-status") {
            principal = MockPrincipal("admin@pointcheck.cl")
        }
        
        mockMvc.get("/api/admin/audit-logs") {
            principal = MockPrincipal("admin@pointcheck.cl")
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].action") { value("DESACTIVAR") }
            jsonPath("$[0].performedByEmail") { value("admin@pointcheck.cl") }
        }
    }
}
