package com.duoc.app.features.report.controller

import com.duoc.app.features.report.dto.ReportSummaryResponse
import com.duoc.app.features.report.service.ReportService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/reports")
@CrossOrigin("*")
class ReportController(
    private val reportService: ReportService
) {

    @GetMapping("/summary/specialist/{specialistId}")
    fun getSummaryBySpecialist(@PathVariable specialistId: Long): ResponseEntity<ReportSummaryResponse> {
        return ResponseEntity.ok(reportService.getSummaryBySpecialist(specialistId))
    }
}
