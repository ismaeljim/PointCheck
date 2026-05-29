package com.duoc.app.features.professionalprofile.repository

import com.duoc.app.features.professionalprofile.model.ProfessionalProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProfessionalProfileRepository : JpaRepository<ProfessionalProfile, String> {

    fun findByUser_Id(userId: String): ProfessionalProfile?

    fun existsByUser_Id(userId: String): Boolean

    fun findByActiveTrue(): List<ProfessionalProfile>
<<<<<<< Updated upstream
    fun findByCategoryIdAndActiveTrue(categoryId: Long): List<ProfessionalProfile>
}
=======
}
>>>>>>> Stashed changes
