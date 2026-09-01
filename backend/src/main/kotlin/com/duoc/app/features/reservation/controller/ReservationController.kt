package com.duoc.app.features.reservation.controller

import com.duoc.app.features.reservation.dto.AvailabilityResponse
import com.duoc.app.features.reservation.dto.ReservationRequest
import com.duoc.app.features.reservation.dto.ReservationResponse
import com.duoc.app.features.reservation.dto.ReservationStatusUpdateRequest
import com.duoc.app.features.reservation.model.ReservationStatus
import com.duoc.app.features.reservation.service.ReservationService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

/**
 * Controlador REST para la gestión de reservaciones (citas).
 * Permite la creación, consulta de disponibilidad y gestión de estados de las citas
 * tanto para clientes como para especialistas.
 */
@RestController("featureReservationController")
@RequestMapping("/api/reservations")
class ReservationController(
    private val reservationService: ReservationService
) {

    /**
     * Crea una nueva reservación en el sistema.
     *
     * @param request Datos de la reservación a crear.
     * @return Respuesta con los detalles de la reservación creada.
     */
    @PostMapping
    fun create(@RequestBody request: ReservationRequest): ResponseEntity<ReservationResponse> {
        return ResponseEntity.ok(reservationService.create(request))
    }

    /**
     * Consulta la disponibilidad horaria de un especialista para una fecha específica.
     *
     * @param specialistProfileId ID del perfil profesional del especialista.
     * @param date Fecha a consultar en formato ISO.
     * @return Respuesta con la lista de horarios disponibles.
     */
    @GetMapping("/availability")
    fun getAvailability(
        @RequestParam specialistProfileId: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<AvailabilityResponse> {
        return ResponseEntity.ok(reservationService.getAvailability(specialistProfileId, date))
    }

    /**
     * Obtiene todas las reservaciones registradas en el sistema.
     * Acceso restringido a administradores para auditoría global.
     *
     * @return Lista de todas las reservaciones.
     */
    @GetMapping
    // @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ADMIN')")
    fun getAll(): ResponseEntity<List<ReservationResponse>> {
        return ResponseEntity.ok(reservationService.getAll())
    }

    /**
     * Obtiene todas las reservaciones asociadas a un cliente.
     *
     * @param clientId ID del cliente.
     * @return Lista de reservaciones del cliente.
     */
    @GetMapping("/client/{clientId}")
    fun getByClient(@PathVariable clientId: String): ResponseEntity<List<ReservationResponse>> {
        return ResponseEntity.ok(reservationService.getByClient(clientId))
    }

    /**
     * Obtiene las próximas reservaciones pendientes para un cliente.
     *
     * @param clientId ID del cliente.
     * @return Lista de reservaciones próximas.
     */
    @GetMapping("/client/{clientId}/upcoming")
    fun getUpcomingByClient(@PathVariable clientId: String): ResponseEntity<List<ReservationResponse>> {
        return ResponseEntity.ok(reservationService.getUpcomingByClient(clientId))
    }

    /**
     * Obtiene todas las reservaciones asignadas a un especialista.
     *
     * @param specialistProfileId ID del perfil profesional del especialista.
     * @return Lista de reservaciones del especialista.
     */
    @GetMapping("/specialist/{specialistProfileId}")
    fun getBySpecialist(@PathVariable specialistProfileId: String): ResponseEntity<List<ReservationResponse>> {
        return ResponseEntity.ok(reservationService.getBySpecialist(specialistProfileId))
    }

    /**
     * Obtiene las reservaciones programadas para el día de hoy para un especialista.
     *
     * @param specialistProfileId ID del perfil profesional del especialista.
     * @return Lista de reservaciones de hoy.
     */
    @GetMapping("/specialist/{specialistProfileId}/today")
    fun getTodayBySpecialist(@PathVariable specialistProfileId: String): ResponseEntity<List<ReservationResponse>> {
        return ResponseEntity.ok(reservationService.getTodayBySpecialist(specialistProfileId))
    }

    /**
     * Obtiene el historial de reservaciones de un cliente.
     *
     * @param clientId ID del cliente.
     * @return Lista de reservaciones históricas.
     */
    @GetMapping("/client/{clientId}/history")
    fun getHistoryByClient(@PathVariable clientId: String): ResponseEntity<List<ReservationResponse>> {
        // En el futuro, esto podría filtrar por estados COMPLETED/CANCELLED
        return ResponseEntity.ok(reservationService.getByClient(clientId))
    }

    /**
     * Actualiza el estado de una reservación.
     *
     * @param id ID de la reservación.
     * @param request Objeto que contiene el nuevo estado.
     * @return Reservación con el estado actualizado.
     */
    @PutMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: String,
        @RequestBody request: ReservationStatusUpdateRequest
    ): ResponseEntity<ReservationResponse> {
        return ResponseEntity.ok(reservationService.updateStatus(id, request.status))
    }

    /**
     * Cancela una reservación existente.
     *
     * @param id ID de la reservación a cancelar.
     * @param userId ID del usuario que solicita la cancelación (Header de seguridad).
     * @return Reservación cancelada.
     */
    @PutMapping("/{id}/cancel")
    fun cancel(
        @PathVariable id: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ReservationResponse> {
        return ResponseEntity.ok(reservationService.cancel(id, userId))
    }

    /**
     * Confirma el pago de una reservación.
     *
     * @param id ID de la reservación pagada.
     * @param userId ID del usuario que confirma el pago (Header de seguridad).
     * @return Reservación con estado de pago actualizado.
     */
    @PutMapping("/{id}/confirm-payment")
    fun confirmPayment(
        @PathVariable id: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ReservationResponse> {
        return ResponseEntity.ok(reservationService.confirmPayment(id, userId))
    }
}
