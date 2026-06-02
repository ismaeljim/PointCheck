package com.duoc.app.features.service.repository

import com.duoc.app.features.service.model.ServiceOffering
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
// CAMBIO: Hereda de String en lugar de Long
interface ServiceOfferingRepository : JpaRepository<ServiceOffering, String> {

    // CAMBIO: El parámetro ahora debe ser String para el UUID del perfil
    fun findByProfessionalProfile_Id(professionalProfileId: String): List<ServiceOffering>

    fun findByActiveTrue(): List<ServiceOffering>
}