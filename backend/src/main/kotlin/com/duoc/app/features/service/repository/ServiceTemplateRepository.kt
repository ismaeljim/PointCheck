package com.duoc.app.features.service.repository

import com.duoc.app.features.service.model.ServiceTemplate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ServiceTemplateRepository : JpaRepository<ServiceTemplate, Long> {
    fun findByCategory_IdAndActiveTrue(categoryId: Long): List<ServiceTemplate>
}
