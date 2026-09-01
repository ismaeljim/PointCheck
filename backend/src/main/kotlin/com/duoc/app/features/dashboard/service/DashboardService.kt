package com.duoc.app.features.dashboard.service

import com.duoc.app.features.attention.model.AttentionStatus
import com.duoc.app.features.attention.repository.AttentionRepository
import com.duoc.app.features.billing.model.PaymentStatus
import com.duoc.app.features.billing.repository.BillingRecordRepository
import com.duoc.app.features.dashboard.dto.DashboardMetricsResponse
import com.duoc.app.features.dashboard.dto.FavoriteSpecialistDto
import com.duoc.app.features.dashboard.dto.NotificationSummaryDto
import com.duoc.app.features.dashboard.dto.ClientDashboardResponse
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
    private val serviceOfferingRepository: com.duoc.app.features.service.repository.ServiceOfferingRepository,
    private val userRepository: com.duoc.app.features.user.repository.UserRepository,
    private val notificationService: com.duoc.app.features.notification.service.NotificationService,
    private val auditLogRepository: com.duoc.app.features.admin.repository.AuditLogRepository
) {

    /**
     * Recupera un usuario por su email.
     * Utilizado para validaciones de seguridad en los controladores.
     */
    fun getUserByEmail(email: String): com.duoc.app.features.user.model.User {
        return userRepository.findByEmailWithProfile(email)
            ?: throw IllegalArgumentException("Usuario no encontrado")
    }

    /**
     * Genera un resumen detallado de desempeño para un especialista, usualmente para la sección de Reportes.
     *
     * Calcula métricas clave como volumen de reservas, atenciones completadas, promedio de
     * duración de citas e indicadores financieros (pagado vs pendiente) del mes en curso.
     *
     * @param userId ID del usuario especialista.
     * @return [ReportSummaryResponse] con la consolidación de métricas de productividad y finanzas.
     */
    fun getReportSummary(userIdOrProfileId: String): com.duoc.app.features.report.dto.ReportSummaryResponse {
        val now = LocalDateTime.now()
        val todayStart = now.toLocalDate().atStartOfDay()
        val todayEnd = now.toLocalDate().atTime(LocalTime.MAX)
        val monthStart = now.withDayOfMonth(1).toLocalDate().atStartOfDay()

        val profile = profileRepository.findById(userIdOrProfileId)
            .orElseGet { profileRepository.findByUser_Id(userIdOrProfileId) }
            ?: throw IllegalArgumentException("Perfil profesional no encontrado para el usuario")
        val profileId = profile.id!!
        
        // Optimizamos usando conteos directos en DB
        val totalReservations = reservationRepository.countBySpecialist_Id(profileId)
        val todayReservations = reservationRepository.countBySpecialist_IdAndReservationStartBetween(profileId, todayStart, todayEnd)
        
        val completedAttentionsCount = attentionRepository.countBySpecialist_IdAndStartedAtBetweenAndStatus(
            profileId, monthStart, todayEnd, AttentionStatus.FINISHED
        )
        val completedReservationsCount = reservationRepository.countBySpecialist_IdAndReservationStartBetweenAndStatus(
            profileId, monthStart, todayEnd, ReservationStatus.COMPLETED
        )

        val totalDurationAttentions = attentionRepository.sumDurationMinutesBySpecialistAndStartedAtBetweenAndStatus(
            profileId, monthStart, todayEnd, AttentionStatus.FINISHED
        )
        val totalDurationReservations = reservationRepository.sumServiceDurationMinutesBySpecialistAndReservationStartBetweenAndStatus(
            profileId, monthStart, todayEnd, ReservationStatus.COMPLETED
        )

        val totalPerformed = (completedAttentionsCount + completedReservationsCount).toInt()

        val totalCharged = billingRecordRepository.sumAmountBySpecialistAndCreatedAtBetweenAndStatus(
            profileId, monthStart, todayEnd, PaymentStatus.PAID
        )

        val pendingAmount = billingRecordRepository.sumAmountBySpecialistAndCreatedAtBetweenAndStatus(
            profileId, monthStart, todayEnd, PaymentStatus.PENDING
        )

        val paidBillingCount = billingRecordRepository.countByReservation_Specialist_IdAndCreatedAtBetweenAndStatus(
            profileId, monthStart, todayEnd, PaymentStatus.PAID
        )

        val pendingBillingCount = billingRecordRepository.countByReservation_Specialist_IdAndCreatedAtBetweenAndStatus(
            profileId, monthStart, todayEnd, PaymentStatus.PENDING
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
            specialty = profile.specialty ?: ""
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
        val cleanRole = role.replace("ROLE_", "").uppercase()
        
        return when (cleanRole) {
            "ADMIN" -> {
                val reservationsToday = try {
                    reservationRepository.countByReservationStartBetween(
                        LocalDateTime.now().toLocalDate().atStartOfDay(),
                        LocalDateTime.now().toLocalDate().atTime(LocalTime.MAX)
                    )
                } catch (e: Exception) { 0L }

                val totalClients = try { userRepository.countByRole(com.duoc.app.features.user.model.UserRole.CLIENT) } catch (e: Exception) { 0L }
                val totalSpecialists = try { userRepository.countByRole(com.duoc.app.features.user.model.UserRole.SPECIALIST) } catch (e: Exception) { 0L }
                
                val billingAll = try { billingRecordRepository.findAll() } catch (e: Exception) { emptyList() }
                val totalRevenuePaid = billingAll.filter { it.status == PaymentStatus.PAID }.sumOf { it.amount?.toDouble() ?: 0.0 }
                val pendingRevenue = billingAll.filter { it.status == PaymentStatus.PENDING }.sumOf { it.amount?.toDouble() ?: 0.0 }
                
                val todayStart = LocalDateTime.now().toLocalDate().atStartOfDay()
                val todayEnd = LocalDateTime.now().toLocalDate().atTime(LocalTime.MAX)
                val alertsToday = try { auditLogRepository.countByTimestampBetween(todayStart, todayEnd) } catch (e: Exception) { 0L }

                // Series para ADMIN (Ingresos últimos 7 días) con manejo de errores
                val revenueSeries = (6 downTo 0).map { daysAgo ->
                    val date = LocalDate.now().minusDays(daysAgo.toLong())
                    val dayStart = date.atStartOfDay()
                    val dayEnd = date.atTime(LocalTime.MAX)
                    val dailyRevenue = billingAll
                        .filter { 
                            it.status == PaymentStatus.PAID && 
                            it.createdAt != null && 
                            it.createdAt.isAfter(dayStart) && 
                            it.createdAt.isBefore(dayEnd) 
                        }
                        .sumOf { it.amount?.toDouble() ?: 0.0 }
                    com.duoc.app.features.dashboard.dto.ChartDataDto(date.dayOfWeek.name.take(3), dailyRevenue)
                }

                DashboardMetricsResponse(
                    appointmentsToday = reservationsToday.toInt(),
                    totalUsers = (totalClients + totalSpecialists).toInt(),
                    totalRevenue = totalRevenuePaid,
                    pendingRevenue = pendingRevenue,
                    activeSpecialists = totalSpecialists.toInt(),
                    systemAlerts = alertsToday.toInt(),
                    subscriptionStatus = "ADMIN_MODE",
                    revenueSeries = revenueSeries
                )
            }
            "CLIENT" -> {
                val upcoming = reservationRepository.findByClient_IdAndReservationStartAfter(userId, now)
                val all = reservationRepository.findByClient_IdOrderByCreatedAtDesc(userId)
                
                DashboardMetricsResponse(
                    upcomingReservationsCount = upcoming.size,
                    recentReservationsCount = all.size,
                    lastReservationStatus = all.maxByOrNull { it.updatedAt ?: it.createdAt }?.status?.name ?: ""
                )
            }
            else -> {
                val profile = profileRepository.findById(userId)
                    .orElseGet { profileRepository.findByUser_Id(userId) }
                if (profile == null) return DashboardMetricsResponse(isProfileComplete = false)
                val profileId = profile.id!!

                // Validación de servicio mínimo: Un perfil no está completo si no tiene servicios que ofrecer
                val hasServices = serviceOfferingRepository.findByProfessionalProfile_Id(profileId).isNotEmpty()
                if (!hasServices) return DashboardMetricsResponse(isProfileComplete = false)

                val now = LocalDateTime.now()
                val todayStart = now.toLocalDate().atStartOfDay()
                val todayEnd = now.toLocalDate().atTime(LocalTime.MAX)
                val monthStart = now.withDayOfMonth(1).toLocalDate().atStartOfDay()
                val monthEnd = now.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX)

                // 1. Métricas de Reservas (Hoy y Mes)
                val appointmentsToday = reservationRepository.countBySpecialist_IdAndReservationStartBetween(
                    profileId, todayStart, todayEnd
                ).toInt()
                
                val appointmentsMonth = reservationRepository.countBySpecialist_IdAndReservationStartBetween(
                    profileId, monthStart, monthEnd
                ).toInt()

                // 2. Métricas de Atención y Duración (Consistente con Reportes)
                val completedAttentionsCount = attentionRepository.countBySpecialist_IdAndStartedAtBetweenAndStatus(
                    profileId, monthStart, monthEnd, com.duoc.app.features.attention.model.AttentionStatus.FINISHED
                )
                
                val completedReservationsCount = reservationRepository.countBySpecialist_IdAndReservationStartBetweenAndStatus(
                    profileId, monthStart, monthEnd, ReservationStatus.COMPLETED
                )

                val totalDurationAttentions = attentionRepository.sumDurationMinutesBySpecialistAndStartedAtBetweenAndStatus(
                    profileId, monthStart, monthEnd, com.duoc.app.features.attention.model.AttentionStatus.FINISHED
                )
                
                val totalDurationReservations = reservationRepository.sumServiceDurationMinutesBySpecialistAndReservationStartBetweenAndStatus(
                    profileId, monthStart, monthEnd, com.duoc.app.features.reservation.model.ReservationStatus.COMPLETED
                )

                val totalPerformed = (completedAttentionsCount + completedReservationsCount).toInt()
                val avgDuration = if (totalPerformed > 0) {
                    (totalDurationAttentions + totalDurationReservations).toDouble() / totalPerformed
                } else 0.0

                // 3. Métricas Financieras (Usando la nueva consulta por fecha de reserva)
                val pendingAmount = billingRecordRepository.sumAmountBySpecialistAndReservationDateBetweenAndStatus(
                    profileId, monthStart, monthEnd, PaymentStatus.PENDING
                ).toDouble()

                val paidAmount = billingRecordRepository.sumAmountBySpecialistAndReservationDateBetweenAndStatus(
                    profileId, monthStart, monthEnd, PaymentStatus.PAID
                ).toDouble()

                // Series para ESPECIALISTA (Citas últimos 7 días)
                val activitySeries = (6 downTo 0).map { daysAgo ->
                    val date = LocalDate.now().minusDays(daysAgo.toLong())
                    val dayStart = date.atStartOfDay()
                    val dayEnd = date.atTime(LocalTime.MAX)
                    val count = reservationRepository.countBySpecialist_IdAndReservationStartBetween(profileId, dayStart, dayEnd)
                    com.duoc.app.features.dashboard.dto.ChartDataDto(date.dayOfWeek.name.take(3), count.toDouble())
                }

                DashboardMetricsResponse(
                    appointmentsToday = appointmentsToday,
                    appointmentsMonth = appointmentsMonth,
                    totalAttentionsPerformed = totalPerformed,
                    averageDurationMinutes = avgDuration,
                    pendingBillingAmount = pendingAmount,
                    paidBillingAmount = paidAmount,
                    subscriptionStatus = "ACTIVE",
                    specialty = profile.specialty ?: "",
                    activitySeries = activitySeries
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
    fun getClientDashboard(userId: String): ClientDashboardResponse {
        val now = LocalDateTime.now()
        
        // Obtenemos la cita futura más cercana que no esté cancelada
        val nextAppointment = reservationRepository.findByClient_IdAndReservationStartAfter(userId, now)
            .filter { it.status != com.duoc.app.features.reservation.model.ReservationStatus.CANCELLED }
            .minByOrNull { it.reservationStart }
            ?.toResponse()

        // Identificamos especialistas recurrentes para sugerencias
        val favoriteSpecialists = reservationRepository.findByClient_IdOrderByCreatedAtDesc(userId)
            .groupBy { it.specialist }
            .map { (specialist, reservations) ->
                FavoriteSpecialistDto(
                    specialistProfileId = specialist.id!!,
                    name = specialist.displayName,
                    specialty = specialist.specialty ?: "",
                    visitCount = reservations.size.toLong()
                )
            }
            .sortedByDescending { it.visitCount }
            .take(3)

        // Resumen de notificaciones recientes para la campana de alertas
        val recentNotifications = notificationService.getRecentNotifications(userId, 3)
            .map { 
                NotificationSummaryDto(
                    id = it.id!!,
                    title = it.title ?: "",
                    message = it.message ?: "",
                    type = it.type.name,
                    isRead = it.isRead,
                    createdAt = it.createdAt
                )
            }

        return ClientDashboardResponse(
            nextAppointment = nextAppointment,
            favoriteSpecialists = favoriteSpecialists,
            recentNotifications = recentNotifications
        )
    }

    private fun com.duoc.app.features.reservation.model.Reservation.toResponse(): com.duoc.app.features.reservation.dto.ReservationResponse {
        val profProfile = this.specialist
        return com.duoc.app.features.reservation.dto.ReservationResponse(
            id = this.id!!,
            client = this.client.toSummaryDto(),
            specialist = this.specialist.user.toSummaryDto(),
            specialistProfileId = profProfile.id ?: "",
            city = profProfile.city ?: "",
            address = profProfile.address ?: "",
            serviceId = this.service?.id ?: "",
            serviceName = this.service?.name ?: "",
            categoryIcon = profProfile.category?.iconKey ?: "",
            categoryColor = profProfile.category?.colorHex ?: "",
            isAtHome = this.service?.isAtHome ?: false,
            reservationStart = this.reservationStart,
            reservationEnd = this.reservationEnd,
            status = this.status,
            notes = this.notes ?: "",
            createdAt = this.createdAt
        )
    }
}
