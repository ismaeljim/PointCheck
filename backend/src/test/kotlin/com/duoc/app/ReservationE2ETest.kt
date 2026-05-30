package com.duoc.app

import com.duoc.app.features.attention.repository.AttentionRepository
import com.duoc.app.features.billing.repository.BillingRecordRepository
import com.duoc.app.features.notification.repository.NotificationRepository
import com.duoc.app.features.professionalprofile.model.ProfessionalProfile
import com.duoc.app.features.professionalprofile.repository.ProfessionalProfileRepository
import com.duoc.app.features.reservation.dto.ReservationRequest
import com.duoc.app.features.reservation.model.ReservationStatus
import com.duoc.app.features.reservation.repository.ReservationRepository
import com.duoc.app.features.service.model.ServiceOffering
import com.duoc.app.features.service.repository.ServiceOfferingRepository
import com.duoc.app.features.subscription.repository.SubscriptionRepository
import com.duoc.app.features.user.model.User
import com.duoc.app.features.user.model.UserRole
import com.duoc.app.features.user.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.get
import java.time.LocalDateTime

import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservationE2ETest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val userRepository: UserRepository,
    private val reservationRepository: ReservationRepository,
    private val profileRepository: ProfessionalProfileRepository,
    private val serviceRepository: ServiceOfferingRepository,
    private val notificationRepository: NotificationRepository,
    private val attentionRepository: AttentionRepository,
    private val billingRecordRepository: BillingRecordRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val objectMapper: ObjectMapper
) {

    private lateinit var specialistAsClient: User
    private lateinit var specialistAsProvider: User
    private lateinit var service: ServiceOffering

    @BeforeEach
    fun setup() {
        billingRecordRepository.deleteAll()
        attentionRepository.deleteAll()
        notificationRepository.deleteAll()
        reservationRepository.deleteAll()
        subscriptionRepository.deleteAll()
        serviceRepository.deleteAll()
        profileRepository.deleteAll()
        userRepository.deleteAll()

        // 1. Crear Especialista A (el que actuará como cliente)
        specialistAsClient = userRepository.save(
            User(
                name = "Especialista Cliente",
                email = "cliente@specialist.com",
                password = "password",
                rut = "22222222-2",
                phone = "+56911111111",
                role = UserRole.SPECIALIST
            )
        )

        // 2. Crear Especialista B (el que provee el servicio)
        specialistAsProvider = userRepository.save(
            User(
                name = "Especialista Provee",
                email = "provee@specialist.com",
                password = "password",
                rut = "33333333-3",
                phone = "+56922222222",
                role = UserRole.SPECIALIST
            )
        )

        // 3. Crear Perfil Profesional y Servicio para el Especialista B
        val profile = profileRepository.save(
            ProfessionalProfile(
                user = specialistAsProvider,
                displayName = "Clinica Provee",
                city = "Santiago",
                address = "Calle Falsa 123"
            )
        )

        service = serviceRepository.save(
            ServiceOffering(
                professionalProfile = profile,
                name = "Corte de Cabello",
                active = true
            )
        )
    }

    @Test
    fun `Un especialista puede agendar una cita con otro especialista como cliente`() {
        val reservationStart = LocalDateTime.now().plusDays(1).withNano(0)
        val request = ReservationRequest(
            clientId = specialistAsClient.id.toString(),
            specialistId = specialistAsProvider.id.toString(),
            serviceId = service.id,
            reservationStart = reservationStart,
            notes = "Test de flujo especialista-como-cliente"
        )

        // Ejecutar POST para crear reserva
        mockMvc.post("/api/reservations") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.clientId") { value(specialistAsClient.id) }
            jsonPath("$.specialistId") { value(specialistAsProvider.id) }
            jsonPath("$.status") { value(ReservationStatus.PENDING.name) }
            jsonPath("$.specialistName") { value(specialistAsProvider.name) }
            jsonPath("$.address") { value("Calle Falsa 123") }
        }

        // Verificar que aparece en la lista de citas del cliente (Especialista A)
        mockMvc.get("/api/reservations/client/${specialistAsClient.id}") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(1) }
            jsonPath("$[0].specialistId") { value(specialistAsProvider.id) }
        }

        // Verificar que aparece en la agenda del proveedor (Especialista B)
        mockMvc.get("/api/reservations/specialist/${specialistAsProvider.id}") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(1) }
            jsonPath("$[0].clientId") { value(specialistAsClient.id) }
        }
    }
}
