package com.duoc.app.features.service.repository

import com.duoc.app.features.service.model.ServiceOffering
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ServiceOfferingRepository : JpaRepository<ServiceOffering, Long> {
    fun findByProfessionalProfileId(professionalProfileId: Long): List<ServiceOffering>
    fun findByProfessionalProfileIdAndActiveTrue(professionalProfileId: Long): List<ServiceOffering>
    fun findByActiveTrue(): List<ServiceOffering>
}
