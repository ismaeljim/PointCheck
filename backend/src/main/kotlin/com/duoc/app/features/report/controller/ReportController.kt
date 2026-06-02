package com.duoc.app.features.report.controller

import com.duoc.app.features.report.dto.MonthlyReportResponse
import com.duoc.app.features.report.dto.ReportSummaryResponse
import com.duoc.app.features.report.dto.WeeklyReportResponse
import com.duoc.app.features.report.service.ReportService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * AUDITORÍA TÉCNICA: API de Business Intelligence (BI)
 * 
 * Expone los endpoints para la generación de métricas y exportación de datos legibles por humanos.
 * 
 * Hallazgos de Seguridad/Arquitectura:
 * 1. [CRÍTICO] Falta de Autorización: Actualmente los reportes son accesibles solo con el ID de usuario. 
 *    Se debe implementar Spring Security para asegurar que un especialista solo acceda a SUS propios datos.
 * 2. [OK] Content-Type: Los endpoints de exportación manejan correctamente el tipo MIME 'text/csv'.
 * 3. [OK] Parámetros Dinámicos: El uso de 'offset' permite una navegación temporal fluida desde el cliente.
 */
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
        @RequestParam(defaultValue = "0") weekOffset: Long,
        @RequestParam(required = false) serviceId: String?
    ): ResponseEntity<WeeklyReportResponse> {
        return ResponseEntity.ok(reportService.getWeeklyReport(userId, weekOffset, serviceId))
    }

    @GetMapping("/monthly/{userId}")
    fun getMonthlyReport(
        @PathVariable userId: String,
        @RequestParam(defaultValue = "0") monthOffset: Long,
        @RequestParam(required = false) serviceId: String?
    ): ResponseEntity<MonthlyReportResponse> {
        return ResponseEntity.ok(reportService.getMonthlyReport(userId, monthOffset, serviceId))
    }

    /**
     * AUDITORÍA: Endpoint de Exportación.
     * Genera descarga directa del archivo con cabeceras de disposición de contenido.
     */
    @GetMapping("/export/weekly/{userId}")
    fun exportWeekly(
        @PathVariable userId: String,
        @RequestParam(defaultValue = "0") weekOffset: Long,
        @RequestParam(required = false) serviceId: String?
    ): ResponseEntity<String> {
        val csv = reportService.exportWeeklyCSV(userId, weekOffset, serviceId)
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=reporte_semanal.csv")
            .header("Content-Type", "text/csv; charset=UTF-8")
            .body(csv)
    }

    @GetMapping("/export/monthly/{userId}")
    fun exportMonthly(
        @PathVariable userId: String,
        @RequestParam(defaultValue = "0") monthOffset: Long,
        @RequestParam(required = false) serviceId: String?
    ): ResponseEntity<String> {
        val csv = reportService.exportMonthlyCSV(userId, monthOffset, serviceId)
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=reporte_mensual.csv")
            .header("Content-Type", "text/csv; charset=UTF-8")
            .body(csv)
    }
}
