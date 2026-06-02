package com.duoc.app

import com.duoc.app.features.admin.repository.AuditLogRepository
import com.duoc.app.features.admin.repository.GlobalSettingsRepository
import com.duoc.app.features.attention.repository.AttentionRepository
import com.duoc.app.features.billing.repository.BillingRecordRepository
import com.duoc.app.features.notification.repository.NotificationRepository
import com.duoc.app.features.professionalprofile.repository.ProfessionalProfileRepository
import com.duoc.app.features.reservation.repository.ReservationRepository
import com.duoc.app.features.service.repository.CategoryRepository
import com.duoc.app.features.service.repository.ServiceOfferingRepository
import com.duoc.app.features.user.model.User
import com.duoc.app.features.user.model.UserRole
import com.duoc.app.features.user.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

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

    @Autowired
    private lateinit var auditLogRepository: AuditLogRepository

    @Autowired
    private lateinit var settingsRepository: GlobalSettingsRepository

    @Autowired
    private lateinit var notificationRepository: NotificationRepository

    @BeforeEach
    fun setup() {
        // Limpieza en orden inverso de dependencias para evitar violaciones de FK
        billingRecordRepository.deleteAll()
        attentionRepository.deleteAll()
        notificationRepository.deleteAll()
        reservationRepository.deleteAll()
        serviceOfferingRepository.deleteAll()
        professionalProfileRepository.deleteAll()
        categoryRepository.deleteAll()
        auditLogRepository.deleteAll()
        settingsRepository.deleteAll()
        userRepository.deleteAll()

        userRepository.save(
            User(
                email = "test@correo.com",
                name = "Usuario Test",
                password = "1234",
                rut = "11111111-1",
                phone = "+56911111111",
                role = UserRole.CLIENT
            )
        )
    }

    @Test
    fun `GET api users email devuelve usuario`() {
        mockMvc.get("/api/users/email/test@correo.com") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.email") { value("test@correo.com") }
            jsonPath("$.name") { value("Usuario Test") }
        }
    }
}
