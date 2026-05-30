package com.duoc.app.features.admin.repository

import com.duoc.app.features.admin.model.GlobalSettings
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface GlobalSettingsRepository : JpaRepository<GlobalSettings, String> {
    fun findByKey(key: String): Optional<GlobalSettings>
}
