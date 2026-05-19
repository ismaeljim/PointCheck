package com.duoc.app.features.notification.controller

import com.duoc.app.features.notification.service.NotificationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin("*")
class NotificationController(
    private val notificationService: NotificationService
) {

    @PutMapping("/{id}/read")
    fun markAsRead(@PathVariable id: Long): ResponseEntity<Unit> {
        notificationService.markAsRead(id)
        return ResponseEntity.ok().build()
    }
}
