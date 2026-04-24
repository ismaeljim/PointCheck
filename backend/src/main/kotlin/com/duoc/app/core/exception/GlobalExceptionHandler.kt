package com.duoc.app.core.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ApiErrorResponse> {
        val errorResponse = ApiErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = e.message ?: "Argumento inválido",
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(e: NoSuchElementException): ResponseEntity<ApiErrorResponse> {
        val errorResponse = ApiErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            error = "Not Found",
            message = e.message ?: "Recurso no encontrado",
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(e: Exception): ResponseEntity<ApiErrorResponse> {
        val errorResponse = ApiErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = e.message ?: "Ocurrió un error inesperado",
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
