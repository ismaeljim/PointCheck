package com.duoc.app.core.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class, NoSuchElementException::class)
    fun handleNotFound(e: Exception): ResponseEntity<Map<String, Any?>> {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", e.message)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(e: IllegalStateException): ResponseEntity<Map<String, Any?>> {
        return buildResponse(HttpStatus.CONFLICT, "Business Logic Conflict", e.message)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralError(e: Exception): ResponseEntity<Map<String, Any?>> {
        e.printStackTrace()
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Ocurrió un error inesperado")
    }

    private fun buildResponse(status: HttpStatus, error: String, message: String?): ResponseEntity<Map<String, Any?>> {
        val body = mapOf(
            "status" to status.value(),
            "error" to error,
            "message" to message,
            "timestamp" to LocalDateTime.now().toString()
        )
        return ResponseEntity(body, status)
    }
}
