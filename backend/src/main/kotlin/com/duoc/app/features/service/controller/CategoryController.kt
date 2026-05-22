package com.duoc.app.features.service.controller

import com.duoc.app.features.service.dto.CategoryResponse
import com.duoc.app.features.service.dto.ServiceTemplateResponse
import com.duoc.app.features.service.service.CategoryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/categories")
@CrossOrigin("*")
class CategoryController(
    private val categoryService: CategoryService
) {

    @GetMapping
    fun getAllActive(): ResponseEntity<List<CategoryResponse>> {
        return ResponseEntity.ok(categoryService.getAllActive())
    }

    @GetMapping("/{id}/templates")
    fun getTemplatesByCategory(@PathVariable id: Long): ResponseEntity<List<ServiceTemplateResponse>> {
        return ResponseEntity.ok(categoryService.getTemplatesByCategory(id))
    }
}
