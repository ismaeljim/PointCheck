package com.duoc.app.features.service.repository

import com.duoc.app.features.service.model.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<Category, Long> {
    fun findByActiveTrue(): List<Category>
}
