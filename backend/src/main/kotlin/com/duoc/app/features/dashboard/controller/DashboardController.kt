package com.duoc.app.features.dashboard.controller

import com.duoc.app.features.dashboard.dto.DashboardMetricsResponse
import com.duoc.app.features.dashboard.service.DashboardService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/dashboard")
class DashboardController(
    private val dashboardService: DashboardService
) {

    /**
     * Obtiene métricas operativas. 
     * BLOQUEADO para CLIENT: Solo ADMIN y SPECIALIST pueden acceder.
     */
    @GetMapping("/metrics")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SPECIALIST')")
    fun getMetrics(authentication: Authentication): ResponseEntity<DashboardMetricsResponse> {
        val email = authentication.name
        val role = authentication.authorities.first().authority.replace("ROLE_", "")
        
        val user = dashboardService.getUserByEmail(email)
        return ResponseEntity.ok(dashboardService.getMetrics(user.id!!, role))
    }

    /**
     * Dashboard específico para Clientes.
     */
    @GetMapping("/client")
    @PreAuthorize("hasAuthority('CLIENT')")
    fun getClientDashboard(authentication: Authentication): ResponseEntity<com.duoc.app.features.dashboard.dto.ClientDashboardResponse> {
        val email = authentication.name
        val user = dashboardService.getUserByEmail(email)
        return ResponseEntity.ok(dashboardService.getClientDashboard(user.id!!))
    }
}
