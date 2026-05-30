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

@RestController("featureReservationController")
@RequestMapping("/api/reservations")
class ReservationController(
    private val reservationService: ReservationService
) {

    @PostMapping
    fun create(@RequestBody request: ReservationRequest): ResponseEntity<ReservationResponse> {
        return ResponseEntity.ok(reservationService.create(request))
    }

    @GetMapping("/availability")
    fun getAvailability(
        @RequestParam specialistId: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<AvailabilityResponse> {
        return ResponseEntity.ok(reservationService.getAvailability(specialistId, date))
    }

    @GetMapping("/client/{clientId}")
    fun getByClient(@PathVariable clientId: String): ResponseEntity<List<ReservationResponse>> {
        return ResponseEntity.ok(reservationService.getByClient(clientId))
    }

    @GetMapping("/client/{clientId}/upcoming")
    fun getUpcomingByClient(@PathVariable clientId: String): ResponseEntity<List<ReservationResponse>> {
        return ResponseEntity.ok(reservationService.getUpcomingByClient(clientId))
    }

    @GetMapping("/specialist/{specialistId}")
    fun getBySpecialist(@PathVariable specialistId: String): ResponseEntity<List<ReservationResponse>> {
        return ResponseEntity.ok(reservationService.getBySpecialist(specialistId))
    }

    @GetMapping("/specialist/{specialistId}/today")
    fun getTodayBySpecialist(@PathVariable specialistId: String): ResponseEntity<List<ReservationResponse>> {
        return ResponseEntity.ok(reservationService.getTodayBySpecialist(specialistId))
    }

    @GetMapping("/client/{clientId}/history")
    fun getHistoryByClient(@PathVariable clientId: String): ResponseEntity<List<ReservationResponse>> {
        // En el futuro, esto podría filtrar por estados COMPLETED/CANCELLED
        return ResponseEntity.ok(reservationService.getByClient(clientId))
    }

    @PutMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: String,
        @RequestBody request: ReservationStatusUpdateRequest
    ): ResponseEntity<ReservationResponse> {
        return ResponseEntity.ok(reservationService.updateStatus(id, request.status))
    }

    @PutMapping("/{id}/cancel")
    fun cancel(@PathVariable id: String): ResponseEntity<ReservationResponse> {
        return ResponseEntity.ok(reservationService.cancel(id))
    }
}
