package com.duoc.app.features.service.controller

import com.duoc.app.features.service.dto.CategoryResponse
import com.duoc.app.features.service.dto.ServiceTemplateResponse
import com.duoc.app.features.service.service.CategoryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controlador para la gestión de categorías y sus plantillas de servicios asociadas.
 */
@RestController
@RequestMapping("/api/categories")
@CrossOrigin("*")
class CategoryController(
    private val categoryService: CategoryService
) {

    /**
     * Obtiene todas las categorías activas (Salud, Belleza, etc.) para el Onboarding o filtros.
     */
    @GetMapping
    fun getAllActive(): ResponseEntity<List<CategoryResponse>> {
        return ResponseEntity.ok(categoryService.getAllActive())
    }

    /**
     * Recupera las plantillas de servicios predefinidas para una categoría específica.
     */
    @GetMapping("/{id}/templates")
    fun getTemplatesByCategory(@PathVariable id: String): ResponseEntity<List<ServiceTemplateResponse>> {
        return ResponseEntity.ok(categoryService.getTemplatesByCategory(id))
    }
}
