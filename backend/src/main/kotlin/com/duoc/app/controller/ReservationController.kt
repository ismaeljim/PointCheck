package com.duoc.app.controller

import com.duoc.app.model.Reservation
import com.duoc.app.repository.ReservationRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/reservations")
class ReservationController(
    private val reservationRepository: ReservationRepository
) {

    @GetMapping
    fun getByEmail(@RequestParam email: String): ResponseEntity<List<Reservation>> {
        val list = reservationRepository.findByUserEmail(email)
        return ResponseEntity.ok(list)
    }

    @PostMapping
    fun create(@RequestBody reservation: Reservation): ResponseEntity<Reservation> {
        // Forzamos id = 0 para que la DB lo genere
        val toSave = reservation.copy(id = 0)
        val saved = reservationRepository.save(toSave)
        return ResponseEntity.ok(saved)
    }

    // CRUD REAL: UPDATE
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody reservation: Reservation
    ): ResponseEntity<Reservation> {
        if (!reservationRepository.existsById(id)) {
            return ResponseEntity.notFound().build()
        }

        // Asegura que el id venga por path y no dependa del body
        val toSave = reservation.copy(id = id)
        val updated = reservationRepository.save(toSave)
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{id}")
    fun deleteById(@PathVariable id: Long): ResponseEntity<Void> {
        return if (reservationRepository.existsById(id)) {
            reservationRepository.deleteById(id)
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
