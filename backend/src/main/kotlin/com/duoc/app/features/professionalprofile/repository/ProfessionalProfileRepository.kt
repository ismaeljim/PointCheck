package com.duoc.app.features.professionalprofile.repository

import com.duoc.app.features.professionalprofile.model.ProfessionalProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProfessionalProfileRepository : JpaRepository<ProfessionalProfile, Long> {
    fun findByUserId(userId: Long): ProfessionalProfile?
    fun existsByUserId(userId: Long): Boolean
    fun findByActiveTrue(): List<ProfessionalProfile>
}
