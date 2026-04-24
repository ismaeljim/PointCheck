package com.duoc.app.controller

import com.duoc.app.features.reservation.dto.ReservationRequest
import com.duoc.app.features.reservation.dto.ReservationResponse
import com.duoc.app.features.reservation.service.ReservationService
import com.duoc.app.features.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController("legacyReservationController")
@RequestMapping("/api/reservations")
@Deprecated("Migrar a com.duoc.app.features.reservation.controller.ReservationController")
class ReservationController(
    private val reservationService: ReservationService,
    private val userService: UserService
) {

    @GetMapping
    fun getByEmail(@RequestParam email: String): ResponseEntity<List<ReservationResponse>> {
        // TODO: Android debe migrar a GET /api/reservations/client/{id}
        val user = userService.getByEmail(email)
        return ResponseEntity.ok(reservationService.getByClient(user.id))
    }

    @PostMapping
    fun create(@RequestBody request: ReservationRequest): ResponseEntity<ReservationResponse> {
        // TODO: Android debe migrar a usar IDs numéricos en lugar de basarse solo en email
        return ResponseEntity.ok(reservationService.create(request))
    }
}
