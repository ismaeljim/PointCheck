package com.duoc.app.features.service.repository

import com.duoc.app.features.service.model.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repositorio para la gestión de categorías de servicios.
 */
@Repository
interface CategoryRepository : JpaRepository<Category, String> {
    /**
     * Recupera todas las categorías que están marcadas como activas para su uso en la App.
     */
    fun findByActiveTrue(): List<Category>
}
