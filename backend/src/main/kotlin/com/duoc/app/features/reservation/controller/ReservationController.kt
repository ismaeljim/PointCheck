package com.duoc.app.features.reservation.controller

import com.duoc.app.features.reservation.dto.ReservationRequest
import com.duoc.app.features.reservation.dto.ReservationResponse
import com.duoc.app.features.reservation.dto.ReservationStatusUpdateRequest
import com.duoc.app.features.reservation.model.ReservationStatus
import com.duoc.app.features.reservation.service.ReservationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController("featureReservationController")
@RequestMapping("/api/reservations")
class ReservationController(
    private val reservationService: ReservationService
) {

    @PostMapping
    fun create(@RequestBody request: ReservationRequest): ResponseEntity<ReservationResponse> {
        return ResponseEntity.ok(reservationService.create(request))
    }

    @GetMapping("/client/{clientId}")
    fun getByClient(@PathVariable clientId: Long): ResponseEntity<List<ReservationResponse>> {
        return ResponseEntity.ok(reservationService.getByClient(clientId))
    }

    @GetMapping("/client/{clientId}/all")
    fun getByClientAll(@PathVariable clientId: Long): ResponseEntity<List<ReservationResponse>> {
        return ResponseEntity.ok(reservationService.getByClient(clientId))
    }

    @GetMapping("/specialist/{specialistId}")
    fun getBySpecialist(@PathVariable specialistId: Long): ResponseEntity<List<ReservationResponse>> {
        return ResponseEntity.ok(reservationService.getBySpecialist(specialistId))
    }

    @GetMapping("/specialist/{specialistId}/today")
    fun getTodayBySpecialist(@PathVariable specialistId: Long): ResponseEntity<List<ReservationResponse>> {
        return ResponseEntity.ok(reservationService.getTodayBySpecialist(specialistId))
    }

    @GetMapping("/client/{clientId}/upcoming")
    fun getUpcomingByClient(@PathVariable clientId: Long): ResponseEntity<List<ReservationResponse>> {
        return ResponseEntity.ok(reservationService.getUpcomingByClient(clientId))
    }

    @GetMapping("/client/{clientId}/history")
    fun getHistoryByClient(@PathVariable clientId: Long): ResponseEntity<List<ReservationResponse>> {
        println("Requesting history for client: $clientId")
        return ResponseEntity.ok(reservationService.getByClient(clientId))
    }

    @PutMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: Long,
        @RequestBody request: ReservationStatusUpdateRequest
    ): ResponseEntity<ReservationResponse> {
        return ResponseEntity.ok(reservationService.updateStatus(id, request.status))
    }

    @PutMapping("/{id}/cancel")
    fun cancel(@PathVariable id: Long): ResponseEntity<ReservationResponse> {
        return ResponseEntity.ok(reservationService.cancel(id))
    }
}
