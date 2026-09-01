package com.duoc.app.features.billing.controller

import com.duoc.app.features.billing.dto.BillingRecordRequest
import com.duoc.app.features.billing.dto.BillingRecordResponse
import com.duoc.app.features.billing.dto.MarkAsPaidRequest
import com.duoc.app.features.billing.service.BillingService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/billing")
@CrossOrigin("*")
class BillingController(
    private val billingService: BillingService
) {

    @PostMapping
    fun create(@RequestBody request: BillingRecordRequest): ResponseEntity<BillingRecordResponse> {
        return ResponseEntity.ok(billingService.create(request))
    }

    @PutMapping("/{id}/paid")
    fun markAsPaid(
        @PathVariable id: String,
        @RequestBody request: MarkAsPaidRequest
    ): ResponseEntity<BillingRecordResponse> {
        return ResponseEntity.ok(billingService.markAsPaid(id, request))
    }

    @PutMapping("/{id}/cancel")
    fun cancel(@PathVariable id: String): ResponseEntity<BillingRecordResponse> {
        return ResponseEntity.ok(billingService.cancel(id))
    }

    /**
     * Obtiene registros por especialista. 
     * Soporta /specialist/{id} y /specialist (usando el token).
     */
    @GetMapping("/specialist", "/specialist/{specialistProfileId}")
    fun getBySpecialist(
        @PathVariable(required = false) specialistProfileId: String?,
        authentication: Authentication
    ): ResponseEntity<List<BillingRecordResponse>> {
        val id = specialistProfileId ?: billingService.getUserByEmail(authentication.name).id!!
        return ResponseEntity.ok(billingService.getBySpecialist(id))
    }

    @GetMapping("/specialist/{specialistProfileId}/pending")
    fun getPendingBySpecialist(@PathVariable specialistProfileId: String): ResponseEntity<List<BillingRecordResponse>> {
        return ResponseEntity.ok(billingService.getPendingBySpecialist(specialistProfileId))
    }

    @GetMapping("/specialist/{specialistProfileId}/today")
    fun getTodayBySpecialist(@PathVariable specialistProfileId: String): ResponseEntity<List<BillingRecordResponse>> {
        return ResponseEntity.ok(billingService.getTodayBySpecialist(specialistProfileId))
    }
}
