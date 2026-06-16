package com.pointcheck.core.network

import com.pointcheck.features.auth.data.dto.LoginRequestDto
import com.pointcheck.features.auth.data.dto.RegisterRequestDto
import com.pointcheck.features.auth.data.dto.UserResponseDto
import com.pointcheck.features.reservation.data.dto.*
import com.pointcheck.features.profile.data.dto.ProfessionalProfileRequestDto
import com.pointcheck.features.profile.data.dto.ProfessionalProfileResponseDto
import com.pointcheck.features.services.data.dto.ServiceRequestDto
import com.pointcheck.features.services.data.dto.ServiceResponseDto
import com.pointcheck.features.attentions.data.dto.StartAttentionRequestDto
import com.pointcheck.features.attentions.data.dto.FinishAttentionRequestDto
import com.pointcheck.features.attentions.data.dto.AttentionResponseDto
import com.pointcheck.features.billing.data.dto.BillingRecordRequestDto
import com.pointcheck.features.billing.data.dto.BillingRecordResponseDto
import com.pointcheck.features.billing.data.dto.MarkAsPaidRequestDto
import com.pointcheck.features.subscriptions.data.dto.SubscriptionRequestDto
import com.pointcheck.features.subscriptions.data.dto.SubscriptionResponseDto
import com.pointcheck.features.dashboard.data.dto.DashboardMetricsDto
import com.pointcheck.features.dashboard.data.dto.MonthlyReportResponseDto
import com.pointcheck.features.dashboard.data.dto.ReportSummaryResponseDto
import com.pointcheck.features.dashboard.data.dto.WeeklyReportResponseDto
import com.pointcheck.features.external.data.dto.WeatherResponseDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de Retrofit que define los contratos de comunicación con la API del Backend.
 * 
 * Contiene todos los endpoints necesarios para el funcionamiento del ecosistema PointCheck,
 * incluyendo autenticación, gestión de reservas, perfiles profesionales, facturación,
 * suscripciones y reportes.
 *
 * Fuente de verdad: Backend Spring Boot.
 */
interface ApiService {

    // --- Endpoints de Autenticación (Auth) ---
    
    /**
     * Inicia sesión y obtiene los datos del usuario.
     */
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<UserResponseDto>

    /**
     * Registra un nuevo usuario (Cliente o Especialista).
     */
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<UserResponseDto>

    // --- Endpoints de Reservas (Reservation) ---
    
    /**
     * Crea una nueva reserva de servicio.
     */
    @POST("api/reservations")
    suspend fun createReservation(@Body request: ReservationRequestDto): Response<ReservationResponseDto>

    /**
     * Obtiene todas las reservas asociadas a un cliente.
     */
    @GET("api/reservations/client/{clientId}")
    suspend fun getReservationsByClient(@Path("clientId") clientId: String): Response<List<ReservationResponseDto>>

    /**
     * Obtiene las próximas citas activas de un cliente.
     */
    @GET("api/reservations/client/{clientId}/upcoming")
    suspend fun getUpcomingReservationsByClient(@Path("clientId") clientId: String): Response<List<ReservationResponseDto>>

    /**
     * Obtiene el historial de citas pasadas de un cliente.
     */
    @GET("api/reservations/client/{clientId}/history")
    suspend fun getReservationHistoryByClient(@Path("clientId") clientId: String): Response<List<ReservationResponseDto>>

    /**
     * Obtiene todas las reservas asignadas a un especialista.
     */
    @GET("api/reservations/specialist/{specialistId}")
    suspend fun getReservationsBySpecialist(@Path("specialistId") specialistId: String): Response<List<ReservationResponseDto>>

    /**
     * Obtiene las citas programadas para el día de hoy de un especialista.
     */
    @GET("api/reservations/specialist/{specialistId}/today")
    suspend fun getTodayReservationsBySpecialist(@Path("specialistId") specialistId: String): Response<List<ReservationResponseDto>>

    /**
     * Actualiza el estado de una reserva (ej: Confirmada, Cancelada).
     */
    @PUT("api/reservations/{id}/status")
    suspend fun updateReservationStatus(
        @Path("id") id: String,
        @Body request: ReservationStatusUpdateRequestDto
    ): Response<ReservationResponseDto>

    /**
     * Cancela una reserva específica.
     */
    @PUT("api/reservations/{id}/cancel")
    suspend fun cancelReservation(
        @Header("X-User-Id") userId: String,
        @Path("id") id: String
    ): Response<ReservationResponseDto>

    /**
     * Confirma el pago de una reserva.
     */
    @PUT("api/reservations/{id}/confirm-payment")
    suspend fun confirmPayment(
        @Header("X-User-Id") userId: String,
        @Path("id") id: String
    ): Response<ReservationResponseDto>

    /**
     * Consulta la disponibilidad de horarios de un especialista para una fecha dada.
     */
    @GET("api/reservations/availability")
    suspend fun getAvailability(
        @Query("specialistId") specialistId: String,
        @Query("date") date: String
    ): Response<AvailabilityResponseDto>

    // --- Endpoints de Perfil Profesional (Professional Profile) ---

    /**
     * Obtiene el perfil profesional completo de un usuario.
     */
    @GET("api/professional-profiles/user/{userId}")
    suspend fun getProfessionalProfileByUserId(@Path("userId") userId: String): Response<ProfessionalProfileResponseDto>

    /**
     * Crea el perfil profesional para un especialista.
     */
    @POST("api/professional-profiles")
    suspend fun createProfessionalProfile(@Body request: ProfessionalProfileRequestDto): Response<ProfessionalProfileResponseDto>

    /**
     * Actualiza los datos de un perfil profesional existente.
     */
    @PUT("api/professional-profiles/{id}")
    suspend fun updateProfessionalProfile(
        @Path("id") id: String,
        @Body request: ProfessionalProfileRequestDto
    ): Response<ProfessionalProfileResponseDto>

    // --- Endpoints de Especialistas y Oferta de Servicios ---

    /**
     * Lista perfiles profesionales activos, opcionalmente filtrados por categoría.
     */
    @GET("api/professional-profiles")
    suspend fun getActiveProfessionalProfiles(
        @Query("categoryId") categoryId: String? = null
    ): Response<List<SpecialistResponseDto>>

    /**
     * Lista los servicios ofrecidos por un perfil profesional específico.
     */
    @GET("api/services/professional-profile/{professionalProfileId}")
    suspend fun getServicesByProfessionalProfileId(@Path("professionalProfileId") professionalProfileId: String): Response<List<ServiceResponseDto>>

    /**
     * Registra un nuevo servicio en la oferta de un profesional.
     */
    @POST("api/services")
    suspend fun createService(@Body request: ServiceRequestDto): Response<ServiceResponseDto>

    /**
     * Actualiza un servicio existente.
     */
    @PUT("api/services/{id}")
    suspend fun updateService(
        @Path("id") id: String,
        @Body request: ServiceRequestDto
    ): Response<ServiceResponseDto>

    /**
     * Elimina lógicamente un servicio.
     */
    @DELETE("api/services/{id}")
    suspend fun deleteService(@Path("id") id: String): Response<Void>

    // --- Endpoints de Atención (Attention) ---

    /**
     * Registra el inicio de una atención presencial o a domicilio.
     */
    @POST("api/attentions/start")
    suspend fun startAttention(
        @Body request: StartAttentionRequestDto
    ): Response<AttentionResponseDto>

    /**
     * Finaliza una atención y gatilla el proceso de facturación automática.
     */
    @PUT("api/attentions/{attentionId}/finish")
    suspend fun finishAttention(
        @Path("attentionId") attentionId: String,
        @Body request: FinishAttentionRequestDto
    ): Response<AttentionResponseDto>

    /**
     * Obtiene las atenciones realizadas o en curso hoy por un especialista.
     */
    @GET("api/attentions/specialist/{specialistId}/today")
    suspend fun getTodayAttentionsBySpecialist(
        @Path("specialistId") specialistId: String
    ): Response<List<AttentionResponseDto>>

    /**
     * Consulta el historial de atenciones recibidas por un cliente.
     */
    @GET("api/attentions/client/{clientId}/history")
    suspend fun getAttentionHistoryByClient(
        @Path("clientId") clientId: String
    ): Response<List<AttentionResponseDto>>

    /**
     * Obtiene una atención por el ID de su reserva.
     */
    @GET("api/attentions/reservation/{reservationId}")
    suspend fun getAttentionByReservation(
        @Path("reservationId") reservationId: String
    ): Response<AttentionResponseDto>

    // --- Endpoints de Facturación (Billing) ---

    /**
     * Crea un registro de cobro manual.
     */
    @POST("api/billing")
    suspend fun createBillingRecord(
        @Body request: BillingRecordRequestDto
    ): Response<BillingRecordResponseDto>

    /**
     * Marca un registro de cobro como pagado.
     */
    @PUT("api/billing/{id}/paid")
    suspend fun markBillingAsPaid(
        @Path("id") id: String,
        @Body request: MarkAsPaidRequestDto
    ): Response<BillingRecordResponseDto>

    /**
     * Cancela un cobro pendiente.
     */
    @PUT("api/billing/{id}/cancel")
    suspend fun cancelBillingRecord(
        @Path("id") id: String
    ): Response<BillingRecordResponseDto>

    /**
     * Lista toda la facturación de un especialista.
     */
    @GET("api/billing/specialist/{specialistId}")
    suspend fun getBillingBySpecialist(
        @Path("specialistId") specialistId: String
    ): Response<List<BillingRecordResponseDto>>

    /**
     * Obtiene cobros pendientes de pago para un especialista.
     */
    @GET("api/billing/specialist/{specialistId}/pending")
    suspend fun getPendingBillingBySpecialist(
        @Path("specialistId") specialistId: String
    ): Response<List<BillingRecordResponseDto>>

    /**
     * Resumen de facturación del día para un especialista.
     */
    @GET("api/billing/specialist/{specialistId}/today")
    suspend fun getTodayBillingBySpecialist(
        @Path("specialistId") specialistId: String
    ): Response<List<BillingRecordResponseDto>>

    // --- Endpoints de Suscripciones (Subscription) ---

    /**
     * Registra una nueva suscripción al sistema PointCheck.
     */
    @POST("api/subscriptions")
    suspend fun createSubscription(
        @Body request: SubscriptionRequestDto
    ): Response<SubscriptionResponseDto>

    /**
     * Obtiene el estado de la suscripción actual de un perfil.
     */
    @GET("api/subscriptions/professional-profile/{professionalProfileId}/current")
    suspend fun getCurrentSubscriptionByProfessionalProfile(
        @Path("professionalProfileId") professionalProfileId: String
    ): Response<SubscriptionResponseDto>

    /**
     * Cancela la suscripción activa.
     */
    @PUT("api/subscriptions/{id}/cancel")
    suspend fun cancelSubscription(
        @Path("id") id: String
    ): Response<SubscriptionResponseDto>

    // --- Endpoints de Dashboard y Reportes ---

    /**
     * Obtiene métricas clave para el tablero principal según el rol.
     */
    @GET("api/dashboard/metrics")
    suspend fun getDashboardMetrics(): Response<DashboardMetricsDto>

    /**
     * Obtiene un resumen consolidado de desempeño para reportes.
     */
    @GET("api/reports/summary/specialist/{specialistId}")
    suspend fun getReportSummaryBySpecialist(
        @Path("specialistId") specialistId: String
    ): Response<ReportSummaryResponseDto>

    /**
     * Obtiene datos para el reporte semanal.
     */
    @GET("api/reports/weekly/{userId}")
    suspend fun getWeeklyReport(
        @Path("userId") userId: String,
        @Query("weekOffset") weekOffset: Int = 0,
        @Query("serviceId") serviceId: String? = null
    ): Response<WeeklyReportResponseDto>

    /**
     * Obtiene datos para el reporte mensual.
     */
    @GET("api/reports/monthly/{userId}")
    suspend fun getMonthlyReport(
        @Path("userId") userId: String,
        @Query("monthOffset") monthOffset: Int = 0,
        @Query("serviceId") serviceId: String? = null
    ): Response<MonthlyReportResponseDto>

    /**
     * Descarga el reporte semanal en formato binario (PDF/Excel).
     */
    @GET("api/reports/export/weekly/{userId}")
    suspend fun exportWeeklyReport(
        @Path("userId") userId: String,
        @Query("weekOffset") weekOffset: Int = 0,
        @Query("serviceId") serviceId: String? = null
    ): Response<ResponseBody>

    /**
     * Descarga el reporte mensual en formato binario.
     */
    @GET("api/reports/export/monthly/{userId}")
    suspend fun exportMonthlyReport(
        @Path("userId") userId: String,
        @Query("monthOffset") monthOffset: Int = 0,
        @Query("serviceId") serviceId: String? = null
    ): Response<ResponseBody>

    // --- Endpoints de Dashboard de Cliente ---

    /**
     * Obtiene la vista consolidada para la Home del cliente.
     */
    @GET("api/dashboard/client/{clientId}")
    suspend fun getClientDashboard(
        @Path("clientId") clientId: String
    ): Response<com.pointcheck.features.dashboard.data.dto.ClientDashboardResponseDto>

    // --- Endpoints de Notificaciones ---

    /**
     * Marca una notificación específica como leída.
     */
    @PUT("api/notifications/{id}/read")
    suspend fun markNotificationAsRead(@Path("id") id: String): Response<Unit>

    // --- Endpoints de Administración (Admin) ---

    /**
     * Lista todos los usuarios registrados (Solo ADMIN).
     */
    @GET("api/admin/users")
    suspend fun getAllUsers(): Response<List<UserResponseDto>>

    /**
     * Activa o desactiva la cuenta de un usuario.
     */
    @PATCH("api/admin/users/{id}/toggle-status")
    suspend fun toggleUserStatus(@Path("id") id: String): Response<UserResponseDto>

    /**
     * Actualiza la información de un usuario desde el panel de administración.
     */
    @PUT("api/admin/users/{id}")
    suspend fun updateAdminUser(
        @Path("id") id: String,
        @Body request: com.pointcheck.features.admin.data.dto.AdminUserUpdateRequestDto
    ): Response<UserResponseDto>

    /**
     * Obtiene el reporte financiero global del sistema.
     */
    @GET("api/admin/reports/financial")
    suspend fun getFinancialReport(): Response<Map<String, Any>>

    /**
     * Obtiene la lista de configuraciones globales del sistema.
     */
    @GET("api/admin/settings")
    suspend fun getSettings(): Response<List<com.pointcheck.features.dashboard.data.dto.GlobalSettingDto>>

    /**
     * Actualiza el valor de una configuración global.
     */
    @POST("api/admin/settings")
    suspend fun updateSetting(
        @Query("key") key: String,
        @Query("value") value: String
    ): Response<com.pointcheck.features.dashboard.data.dto.GlobalSettingDto>

    /**
     * Obtiene los logs de auditoría del sistema.
     */
    @GET("api/admin/audit-logs")
    suspend fun getAuditLogs(): Response<List<com.pointcheck.features.admin.data.dto.AuditLogDto>>

    /**
     * Obtiene todas las reservas del sistema para supervisión global (Solo ADMIN).
     */
    @GET("api/admin/reservations")
    suspend fun getAllReservations(): Response<List<ReservationResponseDto>>

    // --- Endpoints de Usuarios ---

    /**
     * Obtiene un usuario por su ID.
     */
    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") id: String): Response<UserResponseDto>

    /**
     * Actualiza el perfil del usuario.
     */
    @PUT("api/users/{id}")
    suspend fun updateUserProfile(
        @Path("id") id: String,
        @Body request: com.pointcheck.features.auth.data.dto.UserUpdateRequestDto
    ): Response<UserResponseDto>

    /**
     * Actualiza la dirección de un usuario.
     */
    @PUT("api/users/{id}/address")
    suspend fun updateUserAddress(
        @Path("id") id: String,
        @Query("address") address: String
    ): Response<UserResponseDto>

    /**
     * Cambia la contraseña del usuario.
     */
    @PUT("api/users/{id}/password")
    suspend fun changePassword(
        @Path("id") id: String,
        @Body request: com.pointcheck.features.auth.data.dto.ChangePasswordRequestDto
    ): Response<Unit>

    // --- Endpoints de Servicios Externos ---

    /**
     * Obtiene el clima actual para una ciudad.
     */
    @GET("api/external/weather/{city}")
    suspend fun getWeather(@Path("city") city: String): Response<WeatherResponseDto>

    @GET("api/external/place/{placeId}")
    suspend fun getPlaceDetails(@Path("placeId") placeId: String): Response<Any>
}
