package com.duoc.app.features.dashboard.service

import com.duoc.app.features.attention.repository.AttentionRepository
import com.duoc.app.features.billing.model.PaymentStatus
import com.duoc.app.features.billing.repository.BillingRecordRepository
import com.duoc.app.features.dashboard.dto.DashboardMetricsResponse
import com.duoc.app.features.professionalprofile.repository.ProfessionalProfileRepository
import com.duoc.app.features.reservation.repository.ReservationRepository
import com.duoc.app.features.reservation.model.ReservationStatus
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.math.BigDecimal

import com.duoc.app.features.dashboard.dto.ReportSummaryResponse

/**
 * Servicio central para la generación de métricas, estadísticas y estados del Tablero (Dashboard).
 *
 * Provee datos agregados y resúmenes de actividad para los tres roles principales del ecosistema:
 * Cliente, Especialista y Administrador. Para maximizar el rendimiento, el servicio utiliza
 * consultas de agregación directas en la base de datos (COUNT, SUM, AVG) en lugar de procesar
 * colecciones de objetos en memoria.
 *
 * @property reservationRepository Repositorio para métricas de citas.
 * @property attentionRepository Repositorio para métricas de atenciones en curso/finalizadas.
 * @property billingRecordRepository Repositorio para indicadores financieros.
 * @property profileRepository Repositorio para acceder a datos de especialidad y ubicación.
 * @property userRepository Repositorio para estadísticas de crecimiento de usuarios.
 * @property notificationService Servicio para integrar alertas recientes en la vista del dashboard.
 */
@Service
class DashboardService(
    private val reservationRepository: ReservationRepository,
    private val attentionRepository: AttentionRepository,
    private val billingRecordRepository: BillingRecordRepository,
    private val profileRepository: ProfessionalProfileRepository,
    private val userRepository: com.duoc.app.features.user.repository.UserRepository,
    private val notificationService: com.duoc.app.features.notification.service.NotificationService
) {

    /**
     * Genera un resumen detallado de desempeño para un especialista, usualmente para la sección de Reportes.
     *
     * Calcula métricas clave como volumen de reservas, atenciones completadas, promedio de
     * duración de citas e indicadores financieros (pagado vs pendiente) del mes en curso.
     *
     * @param userId ID del usuario especialista.
     * @return [ReportSummaryResponse] con la consolidación de métricas de productividad y finanzas.
     */
    fun getReportSummary(userId: String): com.duoc.app.features.report.dto.ReportSummaryResponse {
        val now = LocalDateTime.now()
        val todayStart = now.toLocalDate().atStartOfDay()
        val todayEnd = now.toLocalDate().atTime(LocalTime.MAX)
        val monthStart = now.withDayOfMonth(1).toLocalDate().atStartOfDay()

        val profile = profileRepository.findByUser_Id(userId)
        
        // Optimizamos usando conteos directos en DB
        val totalReservations = reservationRepository.countBySpecialist_Id(userId)
        val todayReservations = reservationRepository.countBySpecialist_IdAndReservationStartBetween(userId, todayStart, todayEnd)
        
        val completedAttentionsCount = attentionRepository.countBySpecialist_IdAndStartedAtBetweenAndStatus(
            userId, monthStart, todayEnd, com.duoc.app.features.attention.model.AttentionStatus.FINISHED
        )
        val completedReservationsCount = reservationRepository.countBySpecialist_IdAndReservationStartBetweenAndStatus(
            userId, monthStart, todayEnd, ReservationStatus.COMPLETED
        )

        val totalDurationAttentions = attentionRepository.sumDurationMinutesBySpecialistAndStartedAtBetweenAndStatus(
            userId, monthStart, todayEnd, com.duoc.app.features.attention.model.AttentionStatus.FINISHED
        ) ?: 0L
        val totalDurationReservations = reservationRepository.sumServiceDurationMinutesBySpecialistAndReservationStartBetweenAndStatus(
            userId, monthStart, todayEnd, com.duoc.app.features.reservation.model.ReservationStatus.COMPLETED
        ) ?: 0L

        val totalPerformed = (completedAttentionsCount + completedReservationsCount).toInt()

        val totalCharged = billingRecordRepository.sumAmountBySpecialistAndCreatedAtBetweenAndStatus(
            userId, monthStart, todayEnd, PaymentStatus.PAID
        ) ?: BigDecimal.ZERO

        val pendingAmount = billingRecordRepository.sumAmountBySpecialistAndCreatedAtBetweenAndStatus(
            userId, monthStart, todayEnd, PaymentStatus.PENDING
        ) ?: BigDecimal.ZERO

        val paidBillingCount = billingRecordRepository.countBySpecialist_IdAndCreatedAtBetweenAndStatus(
            userId, monthStart, todayEnd, PaymentStatus.PAID
        )

        val pendingBillingCount = billingRecordRepository.countBySpecialist_IdAndCreatedAtBetweenAndStatus(
            userId, monthStart, todayEnd, PaymentStatus.PENDING
        )

        return com.duoc.app.features.report.dto.ReportSummaryResponse(
            totalReservations = totalReservations.toInt(),
            todayReservations = todayReservations.toInt(),
            completedAttentions = totalPerformed,
            averageAttentionMinutes = if (totalPerformed > 0) (totalDurationAttentions + totalDurationReservations).toDouble() / totalPerformed else 0.0,
            totalCharged = totalCharged,
            pendingAmount = pendingAmount,
            paidBillingCount = paidBillingCount.toInt(),
            pendingBillingCount = pendingBillingCount.toInt(),
            specialty = profile?.specialty
        )
    }

    /**
     * Obtiene métricas generales optimizadas según el rol del usuario solicitante.
     *
     * - **ADMIN**: Estadísticas globales del ecosistema (crecimiento de usuarios, ingresos totales del sistema).
     * - **CLIENT**: Resumen de su actividad personal (citas próximas y historial reciente).
     * - **SPECIALIST**: Indicadores de rendimiento operativo (citas del día, facturación mensual, tiempos promedio).
     *
     * @param userId ID del usuario autenticado.
     * @param role Rol del usuario (ADMIN, CLIENT, SPECIALIST).
     * @return [DashboardMetricsResponse] con los KPIs correspondientes al contexto del usuario.
     */
    fun getMetrics(userId: String, role: String): DashboardMetricsResponse {
        val now = LocalDateTime.now()
        
        return when {
            role.equals("ADMIN", ignoreCase = true) -> {
                val totalReservations = reservationRepository.count()
                val totalClients = userRepository.countByRole(com.duoc.app.features.user.model.UserRole.CLIENT)
                val totalSpecialists = userRepository.countByRole(com.duoc.app.features.user.model.UserRole.SPECIALIST)
                val totalRevenue = billingRecordRepository.findByStatus(PaymentStatus.PAID).sumOf { it.amount.toDouble() }

                DashboardMetricsResponse(
                    appointmentsToday = totalReservations.toInt(),
                    totalAttentionsPerformed = totalClients.toInt(),
                    averageDurationMinutes = totalSpecialists.toDouble(),
                    paidBillingAmount = totalRevenue,
                    subscriptionStatus = "ADMIN_MODE"
                )
            }
            role.equals("CLIENT", ignoreCase = true) -> {
                val upcoming = reservationRepository.findByClient_IdAndReservationStartAfter(userId, now)
                val all = reservationRepository.findByClient_Id(userId)
                
                DashboardMetricsResponse(
                    upcomingReservationsCount = upcoming.size,
                    recentReservationsCount = all.size,
                    lastReservationStatus = all.maxByOrNull { it.updatedAt ?: it.createdAt }?.status?.name
                )
            }
            else -> {
                val profile = profileRepository.findByUser_Id(userId)
                if (profile == null) return DashboardMetricsResponse()

                val now = LocalDateTime.now()
                val todayStart = now.toLocalDate().atStartOfDay()
                val todayEnd = now.toLocalDate().atTime(LocalTime.MAX)
                val monthStart = now.withDayOfMonth(1).toLocalDate().atStartOfDay()

                // 1. Métricas de Reservas (Hoy y Mes)
                val appointmentsToday = reservationRepository.countBySpecialist_IdAndReservationStartBetween(
                    userId, todayStart, todayEnd
                ).toInt()
                
                val appointmentsMonth = reservationRepository.countBySpecialist_IdAndReservationStartBetween(
                    userId, monthStart, todayEnd.plusDays(30)
                ).toInt()

                // 2. Métricas de Atención y Duración
                val completedAttentionsCount = attentionRepository.countBySpecialist_IdAndStartedAtBetweenAndStatus(
                    userId, monthStart, todayEnd, com.duoc.app.features.attention.model.AttentionStatus.FINISHED
                )
                
                val completedReservationsCount = reservationRepository.countBySpecialist_IdAndReservationStartBetweenAndStatus(
                    userId, monthStart, todayEnd, ReservationStatus.COMPLETED
                )

                val totalDurationAttentions = attentionRepository.sumDurationMinutesBySpecialistAndStartedAtBetweenAndStatus(
                    userId, monthStart, todayEnd, com.duoc.app.features.attention.model.AttentionStatus.FINISHED
                ) ?: 0L
                
                val totalDurationReservations = reservationRepository.sumServiceDurationMinutesBySpecialistAndReservationStartBetweenAndStatus(
                    userId, monthStart, todayEnd, com.duoc.app.features.reservation.model.ReservationStatus.COMPLETED
                ) ?: 0L

                val totalPerformed = (completedAttentionsCount + completedReservationsCount).toInt()
                val avgDuration = if (totalPerformed > 0) {
                    (totalDurationAttentions + totalDurationReservations).toDouble() / totalPerformed
                } else 0.0

                // 3. Métricas Financieras
                val pendingAmount = billingRecordRepository.sumAmountBySpecialistAndCreatedAtBetweenAndStatus(
                    userId, monthStart, todayEnd, PaymentStatus.PENDING
                )?.toDouble() ?: 0.0

                val paidAmount = billingRecordRepository.sumAmountBySpecialistAndCreatedAtBetweenAndStatus(
                    userId, monthStart, todayEnd, PaymentStatus.PAID
                )?.toDouble() ?: 0.0

                DashboardMetricsResponse(
                    appointmentsToday = appointmentsToday,
                    appointmentsMonth = appointmentsMonth,
                    totalAttentionsPerformed = totalPerformed,
                    averageDurationMinutes = avgDuration,
                    pendingBillingAmount = pendingAmount,
                    paidBillingAmount = paidAmount,
                    subscriptionStatus = "ACTIVE",
                    specialty = profile.specialty
                )
            }
        }
    }

    /**
     * Construye la vista consolidada del tablero para la aplicación móvil del cliente.
     *
     * Proporciona acceso rápido a la cita más próxima, una lista de especialistas frecuentes
     * basada en el historial de reservas para facilitar la re-contratación, y las 3 notificaciones
     * más recientes.
     *
     * @param userId ID del usuario cliente.
     * @return [ClientDashboardResponse] con la información de contexto para la Home de la App.
     */
    fun getClientDashboard(userId: String): com.duoc.app.features.dashboard.dto.ClientDashboardResponse {
        val now = LocalDateTime.now()
        
        // Obtenemos la cita futura más cercana que no esté cancelada
        val nextAppointment = reservationRepository.findByClient_IdAndReservationStartAfter(userId, now)
            .filter { it.status != com.duoc.app.features.reservation.model.ReservationStatus.CANCELLED }
            .minByOrNull { it.reservationStart }
            ?.toResponse()

        // Identificamos especialistas recurrentes para sugerencias
        val favoriteSpecialists = reservationRepository.findByClient_Id(userId)
            .groupBy { it.specialist }
            .map { (specialist, reservations) ->
                val profile = profileRepository.findByUser_Id(specialist.id!!)
                com.duoc.app.features.dashboard.dto.FavoriteSpecialistDto(
                    specialistId = profile?.id ?: specialist.id!!,
                    name = specialist.name,
                    specialty = profile?.specialty,
                    visitCount = reservations.size.toLong()
                )
            }
            .sortedByDescending { it.visitCount }
            .take(3)

        // Resumen de notificaciones recientes para la campana de alertas
        val recentNotifications = notificationService.getRecentNotifications(userId, 3)
            .map { 
                com.duoc.app.features.dashboard.dto.NotificationSummaryDto(
                    id = it.id!!,
                    title = it.title,
                    message = it.message,
                    type = it.type.name,
                    isRead = it.isRead,
                    createdAt = it.createdAt
                )
            }

        return com.duoc.app.features.dashboard.dto.ClientDashboardResponse(
            nextAppointment = nextAppointment,
            favoriteSpecialists = favoriteSpecialists,
            recentNotifications = recentNotifications
        )
    }

    private fun com.duoc.app.features.reservation.model.Reservation.toResponse(): com.duoc.app.features.reservation.dto.ReservationResponse {
        val profProfile = this.service?.professionalProfile
        return com.duoc.app.features.reservation.dto.ReservationResponse(
            id = this.id!!,
            client = this.client.toSummaryDto(),
            specialist = this.specialist.toSummaryDto(),
            city = profProfile?.city,
            address = profProfile?.address,
            serviceId = this.service?.id,
            serviceName = this.service?.name,
            categoryIcon = profProfile?.category?.iconKey,
            categoryColor = profProfile?.category?.colorHex,
            isAtHome = this.service?.isAtHome ?: false,
            reservationStart = this.reservationStart,
            reservationEnd = this.reservationEnd,
            status = this.status,
            notes = this.notes,
            createdAt = this.createdAt
        )
    }
}
