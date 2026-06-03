package com.duoc.app.features.professionalprofile.repository

import com.duoc.app.features.professionalprofile.model.ProfessionalProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repositorio para la gestión de persistencia de Perfiles Profesionales.
 */
@Repository
interface ProfessionalProfileRepository : JpaRepository<ProfessionalProfile, String> {

    /**
     * Recupera el perfil profesional asociado a un ID de usuario.
     */
    fun findByUser_Id(userId: String): ProfessionalProfile?

    /**
     * Verifica si existe un perfil profesional para un usuario dado.
     */
    fun existsByUser_Id(userId: String): Boolean

    /**
     * Lista todos los perfiles profesionales activos.
     */
    fun findByActiveTrue(): List<ProfessionalProfile>

    /**
     * Filtra perfiles profesionales por categoría y estado activo.
     */
    fun findByCategoryIdAndActiveTrue(categoryId: String): List<ProfessionalProfile>
}
