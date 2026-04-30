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
import com.pointcheck.features.dashboard.data.dto.ReportSummaryResponseDto
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de Retrofit para definir los servicios de la API.
 * Fuente de verdad: Backend Spring Boot.
 */
interface ApiService {

    // --- Auth Endpoints ---
    
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<UserResponseDto>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<UserResponseDto>

    // --- Reservation Endpoints ---
    
    @POST("api/reservations")
    suspend fun createReservation(@Body request: ReservationRequestDto): Response<ReservationResponseDto>

    @GET("api/reservations/client/{clientId}")
    suspend fun getReservationsByClient(@Path("clientId") clientId: Long): Response<List<ReservationResponseDto>>

    @GET("api/reservations/client/{clientId}/upcoming")
    suspend fun getUpcomingReservationsByClient(@Path("clientId") clientId: Long): Response<List<ReservationResponseDto>>

    @GET("api/reservations/specialist/{specialistId}")
    suspend fun getReservationsBySpecialist(@Path("specialistId") specialistId: Long): Response<List<ReservationResponseDto>>

    @GET("api/reservations/specialist/{specialistId}/today")
    suspend fun getTodayReservationsBySpecialist(@Path("specialistId") specialistId: Long): Response<List<ReservationResponseDto>>

    @PUT("api/reservations/{id}/status")
    suspend fun updateReservationStatus(
        @Path("id") id: Long,
        @Body request: ReservationStatusUpdateRequestDto
    ): Response<ReservationResponseDto>

    @PUT("api/reservations/{id}/cancel")
    suspend fun cancelReservation(@Path("id") id: Long): Response<ReservationResponseDto>

    // --- Professional Profile Endpoints ---

    @GET("api/professional-profiles/user/{userId}")
    suspend fun getProfessionalProfileByUserId(@Path("userId") userId: Long): Response<ProfessionalProfileResponseDto>

    @POST("api/professional-profiles")
    suspend fun createProfessionalProfile(@Body request: ProfessionalProfileRequestDto): Response<ProfessionalProfileResponseDto>

    @PUT("api/professional-profiles/{id}")
    suspend fun updateProfessionalProfile(
        @Path("id") id: Long,
        @Body request: ProfessionalProfileRequestDto
    ): Response<ProfessionalProfileResponseDto>

    // --- Specialist & Service Endpoints ---

    @GET("api/professional-profiles")
    suspend fun getActiveProfessionalProfiles(): Response<List<SpecialistResponseDto>>

    @GET("api/services/professional-profile/{professionalProfileId}")
    suspend fun getServicesByProfessionalProfileId(@Path("professionalProfileId") professionalProfileId: Long): Response<List<ServiceResponseDto>>

    @POST("api/services")
    suspend fun createService(@Body request: ServiceRequestDto): Response<ServiceResponseDto>

    @PUT("api/services/{id}")
    suspend fun updateService(
        @Path("id") id: Long,
        @Body request: ServiceRequestDto
    ): Response<ServiceResponseDto>

    @DELETE("api/services/{id}")
    suspend fun deleteService(@Path("id") id: Long): Response<Void>

    // --- Attention Endpoints ---

    @POST("api/attentions/start")
    suspend fun startAttention(
        @Body request: StartAttentionRequestDto
    ): Response<AttentionResponseDto>

    @PUT("api/attentions/{attentionId}/finish")
    suspend fun finishAttention(
        @Path("attentionId") attentionId: Long,
        @Body request: FinishAttentionRequestDto
    ): Response<AttentionResponseDto>

    @GET("api/attentions/specialist/{specialistId}/today")
    suspend fun getTodayAttentionsBySpecialist(
        @Path("specialistId") specialistId: Long
    ): Response<List<AttentionResponseDto>>

    @GET("api/attentions/client/{clientId}/history")
    suspend fun getAttentionHistoryByClient(
        @Path("clientId") clientId: Long
    ): Response<List<AttentionResponseDto>>

    // --- Billing Endpoints ---

    @POST("api/billing")
    suspend fun createBillingRecord(
        @Body request: BillingRecordRequestDto
    ): Response<BillingRecordResponseDto>

    @PUT("api/billing/{id}/paid")
    suspend fun markBillingAsPaid(
        @Path("id") id: Long,
        @Body request: MarkAsPaidRequestDto
    ): Response<BillingRecordResponseDto>

    @PUT("api/billing/{id}/cancel")
    suspend fun cancelBillingRecord(
        @Path("id") id: Long
    ): Response<BillingRecordResponseDto>

    @GET("api/billing/specialist/{specialistId}")
    suspend fun getBillingBySpecialist(
        @Path("specialistId") specialistId: Long
    ): Response<List<BillingRecordResponseDto>>

    @GET("api/billing/specialist/{specialistId}/pending")
    suspend fun getPendingBillingBySpecialist(
        @Path("specialistId") specialistId: Long
    ): Response<List<BillingRecordResponseDto>>

    @GET("api/billing/specialist/{specialistId}/today")
    suspend fun getTodayBillingBySpecialist(
        @Path("specialistId") specialistId: Long
    ): Response<List<BillingRecordResponseDto>>

    // --- Subscription Endpoints ---

    @POST("api/subscriptions")
    suspend fun createSubscription(
        @Body request: SubscriptionRequestDto
    ): Response<SubscriptionResponseDto>

    @GET("api/subscriptions/professional-profile/{professionalProfileId}/current")
    suspend fun getCurrentSubscriptionByProfessionalProfile(
        @Path("professionalProfileId") professionalProfileId: Long
    ): Response<SubscriptionResponseDto>

    @PUT("api/subscriptions/{id}/cancel")
    suspend fun cancelSubscription(
        @Path("id") id: Long
    ): Response<SubscriptionResponseDto>

    // --- Dashboard & Reports Endpoints ---

    @GET("api/dashboard/metrics/{userId}")
    suspend fun getDashboardMetrics(
        @Path("userId") userId: Long,
        @Query("role") role: String
    ): Response<DashboardMetricsDto>

    @GET("api/reports/summary/specialist/{specialistId}")
    suspend fun getReportSummaryBySpecialist(
        @Path("specialistId") specialistId: Long
    ): Response<ReportSummaryResponseDto>

    // --- External API Endpoints ---

    @GET("api/external/weather/{city}")
    suspend fun getWeather(@Path("city") city: String): Response<Any>

    @GET("api/external/place/{placeId}")
    suspend fun getPlaceDetails(@Path("placeId") placeId: String): Response<Any>
}
