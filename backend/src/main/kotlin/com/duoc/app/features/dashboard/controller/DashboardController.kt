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
        @PathVariable userId: String,
        @RequestParam role: String
    ): ResponseEntity<DashboardMetricsResponse> {
        return ResponseEntity.ok(dashboardService.getMetrics(userId, role))
    }

    @GetMapping("/client/{userId}")
    fun getClientDashboard(@PathVariable userId: Long): ResponseEntity<com.duoc.app.features.dashboard.dto.ClientDashboardResponse> {
        return ResponseEntity.ok(dashboardService.getClientDashboard(userId))
    }
}
