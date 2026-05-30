package com.duoc.app.features.report.controller

import com.duoc.app.features.report.dto.MonthlyReportResponse
import com.duoc.app.features.report.dto.ReportSummaryResponse
import com.duoc.app.features.report.dto.WeeklyReportResponse
import com.duoc.app.features.report.service.ReportService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/reports")
@CrossOrigin("*")
class ReportController(
    private val reportService: ReportService
) {

    @GetMapping("/summary/specialist/{userId}")
    fun getSummaryBySpecialist(@PathVariable userId: String): ResponseEntity<ReportSummaryResponse> {
        return ResponseEntity.ok(reportService.getSummaryBySpecialist(userId))
    }

    @GetMapping("/weekly/{userId}")
    fun getWeeklyReport(
        @PathVariable userId: String,
        @RequestParam(defaultValue = "0") weekOffset: Long
    ): ResponseEntity<WeeklyReportResponse> {
        return ResponseEntity.ok(reportService.getWeeklyReport(userId, weekOffset))
    }

    @GetMapping("/monthly/{userId}")
    fun getMonthlyReport(
        @PathVariable userId: String,
        @RequestParam(defaultValue = "0") monthOffset: Long
    ): ResponseEntity<MonthlyReportResponse> {
        return ResponseEntity.ok(reportService.getMonthlyReport(userId, monthOffset))
    }

    @GetMapping("/export/weekly/{userId}")
    fun exportWeekly(
        @PathVariable userId: String,
        @RequestParam(defaultValue = "0") weekOffset: Long
    ): ResponseEntity<String> {
        val csv = reportService.exportWeeklyCSV(userId, weekOffset)
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=reporte_semanal.csv")
            .header("Content-Type", "text/csv; charset=UTF-8")
            .body(csv)
    }

    @GetMapping("/export/monthly/{userId}")
    fun exportMonthly(
        @PathVariable userId: String,
        @RequestParam(defaultValue = "0") monthOffset: Long
    ): ResponseEntity<String> {
        val csv = reportService.exportMonthlyCSV(userId, monthOffset)
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=reporte_mensual.csv")
            .header("Content-Type", "text/csv; charset=UTF-8")
            .body(csv)
    }
}
