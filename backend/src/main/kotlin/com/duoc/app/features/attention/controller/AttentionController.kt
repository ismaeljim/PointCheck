package com.duoc.app.features.attention.controller

import com.duoc.app.features.attention.dto.AttentionResponse
import com.duoc.app.features.attention.dto.FinishAttentionRequest
import com.duoc.app.features.attention.dto.StartAttentionRequest
import com.duoc.app.features.attention.service.AttentionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/attentions")
@CrossOrigin("*")
class AttentionController(
    private val attentionService: AttentionService
) {

    @PostMapping("/start")
    fun start(@RequestBody request: StartAttentionRequest): ResponseEntity<AttentionResponse> {
        return ResponseEntity.ok(attentionService.start(request))
    }

    @PutMapping("/{attentionId}/finish")
    fun finish(
        @PathVariable attentionId: String,
        @RequestBody request: FinishAttentionRequest
    ): ResponseEntity<AttentionResponse> {
        return ResponseEntity.ok(attentionService.finish(attentionId, request))
    }

    @GetMapping("/specialist/{specialistId}/today")
    fun getTodayBySpecialist(@PathVariable specialistId: String): ResponseEntity<List<AttentionResponse>> {
        return ResponseEntity.ok(attentionService.getTodayBySpecialist(specialistId))
    }

    @GetMapping("/client/{clientId}/history")
    fun getHistoryByClient(@PathVariable clientId: String): ResponseEntity<List<AttentionResponse>> {
        return ResponseEntity.ok(attentionService.getHistoryByClient(clientId))
    }
}
