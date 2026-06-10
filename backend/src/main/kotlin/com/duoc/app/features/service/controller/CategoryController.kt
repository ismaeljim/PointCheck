package com.duoc.app.features.service.controller

import com.duoc.app.features.service.dto.CategoryResponse
import com.duoc.app.features.service.dto.ServiceTemplateResponse
import com.duoc.app.features.service.service.CategoryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST Controller for managing service categories and their associated templates.
 *
 * This controller allows clients to discover available categories (e.g., Health, Beauty)
 * and the predefined service templates assigned to them, which helps specialists 
 * set up their service catalogs more efficiently.
 */
@RestController
@RequestMapping("/api/categories")
@CrossOrigin("*")
class CategoryController(
    private val categoryService: CategoryService
) {

    /**
     * Retrieves all active categories for use in onboarding or dashboard filters.
     *
     * @return A [ResponseEntity] with a list of active [CategoryResponse] objects.
     */
    @GetMapping
    fun getAllActive(): ResponseEntity<List<CategoryResponse>> {
        return ResponseEntity.ok(categoryService.getAllActive())
    }

    /**
     * Retrieves predefined service templates for a specific category.
     *
     * @param id The ID of the category.
     * @return A [ResponseEntity] with a list of [ServiceTemplateResponse] objects.
     */
    @GetMapping("/{id}/templates")
    fun getTemplatesByCategory(@PathVariable id: String): ResponseEntity<List<ServiceTemplateResponse>> {
        return ResponseEntity.ok(categoryService.getTemplatesByCategory(id))
    }
}
