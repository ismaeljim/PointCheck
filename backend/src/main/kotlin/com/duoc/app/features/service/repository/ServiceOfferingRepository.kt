package com.duoc.app.features.service.repository

import com.duoc.app.features.service.model.ServiceOffering
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
// CAMBIO: Hereda de String en lugar de Long
interface ServiceOfferingRepository : JpaRepository<ServiceOffering, String> {

    @org.springframework.data.jpa.repository.Query("SELECT s FROM ServiceOffering s JOIN FETCH s.professionalProfile pp JOIN FETCH pp.category WHERE s.professionalProfile.id = :professionalProfileId")
    fun findByProfessionalProfile_IdWithDetails(professionalProfileId: String): List<ServiceOffering>

    fun findByProfessionalProfile_Id(professionalProfileId: String): List<ServiceOffering>

    fun findByActiveTrue(): List<ServiceOffering>
}