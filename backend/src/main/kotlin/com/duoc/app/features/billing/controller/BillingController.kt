package com.duoc.app.features.billing.controller

import com.duoc.app.features.billing.dto.BillingRecordRequest
import com.duoc.app.features.billing.dto.BillingRecordResponse
import com.duoc.app.features.billing.dto.MarkAsPaidRequest
import com.duoc.app.features.billing.service.BillingService
import org.springframework.http.ResponseEntity
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
        @PathVariable id: Long,
        @RequestBody request: MarkAsPaidRequest
    ): ResponseEntity<BillingRecordResponse> {
        return ResponseEntity.ok(billingService.markAsPaid(id, request))
    }

    @PutMapping("/{id}/cancel")
    fun cancel(@PathVariable id: Long): ResponseEntity<BillingRecordResponse> {
        return ResponseEntity.ok(billingService.cancel(id))
    }

    @GetMapping("/specialist/{specialistId}")
    fun getBySpecialist(@PathVariable specialistId: Long): ResponseEntity<List<BillingRecordResponse>> {
        return ResponseEntity.ok(billingService.getBySpecialist(specialistId))
    }

    @GetMapping("/specialist/{specialistId}/pending")
    fun getPendingBySpecialist(@PathVariable specialistId: Long): ResponseEntity<List<BillingRecordResponse>> {
        return ResponseEntity.ok(billingService.getPendingBySpecialist(specialistId))
    }

    @GetMapping("/specialist/{specialistId}/today")
    fun getTodayBySpecialist(@PathVariable specialistId: Long): ResponseEntity<List<BillingRecordResponse>> {
        return ResponseEntity.ok(billingService.getTodayBySpecialist(specialistId))
    }
}
