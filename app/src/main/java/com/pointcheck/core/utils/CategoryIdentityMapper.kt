package com.pointcheck.core.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIdentityMapper {

    fun mapIcon(iconKey: String?): ImageVector {
        val key = iconKey?.lowercase()?.trim()
        return when (key) {
            "scissors", "barberia", "peluqueria" -> Icons.Default.ContentCut
            "medical", "medical_services", "salud", "kinesiologia" -> Icons.Default.MedicalServices
            "fitness", "gimnasio" -> Icons.Default.FitnessCenter
            "face", "estetica" -> Icons.Default.Face
            "spa", "bienestar" -> Icons.Default.SelfImprovement
            "home", "domicilio" -> Icons.Default.Home
            else -> Icons.Default.Category
        }
    }

    fun mapColor(colorHex: String?): Color {
        return try {
            if (colorHex.isNullOrBlank()) Color(0xFF9E9E9E) // Gray default
            else Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            Color(0xFF9E9E9E)
        }
    }
}
