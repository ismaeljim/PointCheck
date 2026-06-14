package com.pointcheck.core.util

import com.pointcheck.core.data.dto.UserSummaryDto
import com.pointcheck.features.auth.data.dto.UserResponseDto
import com.pointcheck.features.billing.data.dto.BillingRecordResponseDto
import com.pointcheck.features.reservation.data.dto.ReservationResponseDto
import com.pointcheck.features.attentions.data.dto.AttentionResponseDto
import com.pointcheck.features.dashboard.data.dto.DashboardMetricsDto
import com.pointcheck.features.dashboard.data.dto.ClientDashboardResponseDto
import com.pointcheck.features.dashboard.data.dto.ReportSummaryResponseDto
import com.pointcheck.features.admin.data.dto.AuditLogDto
import com.pointcheck.features.profile.data.dto.ProfessionalProfileResponseDto
import java.util.UUID

/**
 * Proveedor de datos mock para asegurar la estabilidad visual durante el rediseño.
 * Permite que las pantallas funcionen sin dependencia directa del backend.
 */
object MockDataProvider {

    val mockUser = UserResponseDto(
        id = UUID.randomUUID().toString(),
        token = "mock_jwt_token_for_ui_testing",
        name = "Juan Pérez",
        email = "juan.perez@example.com",
        rut = "12.345.678-9",
        phone = "+56 9 1234 5678",
        role = "CLIENT",
        active = true,
        address = "Av. Siempre Viva 123, Santiago"
    )

    private val mockClientSummary = UserSummaryDto(
        id = mockUser.id ?: UUID.randomUUID().toString(),
        name = mockUser.name ?: "Usuario Mock",
        rut = mockUser.rut ?: "00.000.000-0",
        email = mockUser.email ?: "",
        phone = mockUser.phone ?: ""
    )

    private val mockSpecialistSummary = UserSummaryDto(
        id = UUID.randomUUID().toString(),
        name = "Dra. María González",
        rut = "9.876.543-2",
        email = "maria.gonzalez@example.com",
        phone = "+56 9 8765 4321",
        profilePicture = "https://randomuser.me/api/portraits/women/44.jpg"
    )

    val mockReservations = listOf(
        ReservationResponseDto(
            id = UUID.randomUUID().toString(),
            client = mockClientSummary,
            specialist = mockSpecialistSummary,
            city = "Santiago",
            address = "Av. Siempre Viva 123",
            serviceId = "svc_001",
            serviceName = "Consulta General",
            categoryIcon = "medical_services",
            categoryColor = "#4CAF50",
            reservationStart = "2024-05-20T10:00:00Z",
            reservationEnd = "2024-05-20T11:00:00Z",
            status = "CONFIRMED",
            notes = "Paciente con dolor lumbar",
            createdAt = "2024-05-15T09:00:00Z"
        ),
        ReservationResponseDto(
            id = UUID.randomUUID().toString(),
            client = mockClientSummary,
            specialist = mockSpecialistSummary,
            city = "Santiago",
            address = "Av. Siempre Viva 123",
            serviceId = "svc_002",
            serviceName = "Kinesiología",
            categoryIcon = "fitness_center",
            categoryColor = "#2196F3",
            reservationStart = "2024-05-22T15:30:00Z",
            reservationEnd = "2024-05-22T16:30:00Z",
            status = "PENDING",
            notes = "",
            createdAt = "2024-05-16T14:20:00Z"
        )
    )

    val mockBillings = listOf(
        BillingRecordResponseDto(
            id = UUID.randomUUID().toString(),
            reservationId = mockReservations[0].id,
            attentionId = UUID.randomUUID().toString(),
            client = mockClientSummary,
            specialist = mockSpecialistSummary,
            amount = 25000.0,
            currency = "CLP",
            paymentMethod = "CASH",
            status = "PAID",
            paidAt = "2024-05-20T11:15:00Z",
            externalReference = "REF12345",
            notes = "Pago en efectivo en recepción",
            createdAt = "2024-05-20T11:15:00Z"
        ),
        BillingRecordResponseDto(
            id = UUID.randomUUID().toString(),
            reservationId = mockReservations[1].id,
            attentionId = null,
            client = mockClientSummary,
            specialist = mockSpecialistSummary,
            amount = 30000.0,
            currency = "CLP",
            paymentMethod = "",
            status = "PENDING",
            paidAt = null,
            externalReference = "",
            notes = "",
            createdAt = "2024-05-16T14:20:00Z"
        )
    )

    val mockMetrics = DashboardMetricsDto(
        upcomingReservationsCount = 2,
        recentReservationsCount = 5,
        lastReservationStatus = "CONFIRMED",
        appointmentsToday = 1,
        appointmentsMonth = 12,
        totalAttentionsPerformed = 45,
        averageDurationMinutes = 45.0,
        pendingBillingAmount = 30000.0,
        paidBillingAmount = 150000.0,
        subscriptionStatus = "ACTIVE",
        subscriptionPlan = "PREMIUM",
        specialty = "Kinesiología"
    )

    val mockClientDashboard = ClientDashboardResponseDto(
        nextAppointment = mockReservations[0],
        favoriteSpecialists = emptyList(),
        recentNotifications = emptyList()
    )

    val mockReportSummary = ReportSummaryResponseDto(
        specialistId = UUID.randomUUID().toString(),
        totalReservations = 10,
        todayReservations = 2,
        completedAttentions = 8,
        averageAttentionMinutes = 40.0,
        totalCharged = 200000.0,
        pendingAmount = 25000.0,
        paidBillingCount = 7,
        pendingBillingCount = 1,
        specialty = "Medicina General"
    )

    val mockAttentions = listOf(
        AttentionResponseDto(
            id = UUID.randomUUID().toString(),
            reservationId = mockReservations[0].id,
            client = mockClientSummary,
            specialist = mockSpecialistSummary,
            startedAt = "2024-05-20T10:05:00Z",
            finishedAt = "2024-05-20T10:55:00Z",
            durationMinutes = 50,
            status = "COMPLETED",
            observations = "Paciente evoluciona favorablemente al tratamiento.",
            createdAt = "2024-05-20T10:05:00Z"
        )
    )

    val mockUsers = listOf(
        mockUser,
        mockUser.copy(id = UUID.randomUUID().toString(), name = "María García", email = "maria@example.com", role = "SPECIALIST"),
        mockUser.copy(id = UUID.randomUUID().toString(), name = "Carlos Soto", email = "carlos@example.com", role = "CLIENT")
    )

    val mockAuditLogs = listOf(
        AuditLogDto(
            id = UUID.randomUUID().toString(),
            action = "LOGIN",
            performedByEmail = "admin@pointcheck.com",
            performedByName = "Admin",
            targetType = "USER",
            targetId = mockUser.id ?: "",
            targetName = mockUser.name ?: "",
            details = "Acceso exitoso al sistema",
            ipAddress = "192.168.1.1",
            timestamp = "2024-05-20T09:00:00Z"
        ),
        AuditLogDto(
            id = UUID.randomUUID().toString(),
            action = "UPDATE",
            performedByEmail = "admin@pointcheck.com",
            performedByName = "Admin",
            targetType = "RESERVATION",
            targetId = mockReservations[0].id,
            targetName = "Reserva #1",
            details = "Estado cambiado a CONFIRMED",
            ipAddress = "192.168.1.1",
            timestamp = "2024-05-20T10:30:00Z"
        )
    )

    val mockProfessionalProfile = ProfessionalProfileResponseDto(
        id = UUID.randomUUID().toString(),
        userId = mockUser.id ?: "",
        categoryId = "cat_medical",
        displayName = "Dra. María González",
        businessName = "Clínica San José",
        specialty = "Kinesiología Deportiva",
        description = "Especialista en rehabilitación de lesiones deportivas con más de 10 años de experiencia.",
        address = "Av. Providencia 1234, Oficina 501",
        city = "Santiago",
        country = "Chile",
        latitude = -33.4372,
        longitude = -70.6506,
        isVerified = true,
        rating = 4.8f,
        workingHoursJson = "{\"MONDAY\":{\"start\":\"09:00\",\"end\":\"18:00\",\"isActive\":true},\"TUESDAY\":{\"start\":\"09:00\",\"end\":\"18:00\",\"isActive\":true}}",
        defaultSessionDurationMinutes = 45,
        active = true
    )
}
