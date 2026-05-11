package com.duoc.app.features.dashboard.controller

import com.duoc.app.features.dashboard.dto.DashboardMetricsResponse
import com.duoc.app.features.dashboard.service.DashboardService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin("*")
class DashboardController(
    private val dashboardService: DashboardService
) {

    @GetMapping("/metrics/{userId}")
    fun getMetrics(
        @PathVariable userId: Long,
        @RequestParam role: String
    ): ResponseEntity<DashboardMetricsResponse> {
        return ResponseEntity.ok(dashboardService.getMetrics(userId, role))
    }
}
