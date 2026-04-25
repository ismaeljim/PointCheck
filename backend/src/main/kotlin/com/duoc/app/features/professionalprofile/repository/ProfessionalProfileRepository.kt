package com.duoc.app.features.professionalprofile.repository

import com.duoc.app.features.professionalprofile.model.ProfessionalProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProfessionalProfileRepository : JpaRepository<ProfessionalProfile, Long> {
    fun findByUser_Id(userId: Long): ProfessionalProfile?
    fun existsByUser_Id(userId: Long): Boolean
    fun findByActiveTrue(): List<ProfessionalProfile>
}
