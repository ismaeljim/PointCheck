package com.duoc.app.features.service.service

import com.duoc.app.features.service.dto.CategoryResponse
import com.duoc.app.features.service.dto.ServiceTemplateResponse
import com.duoc.app.features.service.model.Category
import com.duoc.app.features.service.model.ServiceTemplate
import com.duoc.app.features.service.repository.CategoryRepository
import com.duoc.app.features.service.repository.ServiceTemplateRepository
import org.springframework.stereotype.Service

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val serviceTemplateRepository: ServiceTemplateRepository
) {

    fun getAllActive(): List<CategoryResponse> {
        return categoryRepository.findByActiveTrue().map { it.toResponse() }
    }

    fun getTemplatesByCategory(categoryId: Long): List<ServiceTemplateResponse> {
        return serviceTemplateRepository.findByCategory_IdAndActiveTrue(categoryId).map { it.toResponse() }
    }

    private fun Category.toResponse() = CategoryResponse(
        id = this.id,
        name = this.name,
        iconKey = this.iconKey,
        colorHex = this.colorHex
    )

    private fun ServiceTemplate.toResponse() = ServiceTemplateResponse(
        id = this.id,
        categoryId = this.category.id,
        name = this.name,
        defaultPrice = this.defaultPrice,
        defaultDuration = this.defaultDuration
    )
}
