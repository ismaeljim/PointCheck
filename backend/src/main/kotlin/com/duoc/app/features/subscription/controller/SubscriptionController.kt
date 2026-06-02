package com.duoc.app.features.subscription.controller

import com.duoc.app.features.subscription.dto.SubscriptionRequest
import com.duoc.app.features.subscription.dto.SubscriptionResponse
import com.duoc.app.features.subscription.service.SubscriptionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/subscriptions")
@CrossOrigin("*")
class SubscriptionController(
    private val subscriptionService: SubscriptionService
) {

    @PostMapping
    fun create(@RequestBody request: SubscriptionRequest): ResponseEntity<SubscriptionResponse> {
        return ResponseEntity.ok(subscriptionService.create(request))
    }

    @GetMapping("/professional-profile/{professionalProfileId}/current")
    fun getCurrentByProfessionalProfile(@PathVariable professionalProfileId: String): ResponseEntity<SubscriptionResponse> {
        val subscription = subscriptionService.getCurrentByProfessionalProfile(professionalProfileId)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(subscription)
    }

    @PutMapping("/{id}/cancel")
    fun cancel(@PathVariable id: String): ResponseEntity<SubscriptionResponse> {
        return ResponseEntity.ok(subscriptionService.cancel(id))
    }
}
