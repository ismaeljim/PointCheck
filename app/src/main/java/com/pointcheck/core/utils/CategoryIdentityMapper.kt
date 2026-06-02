package com.pointcheck.core.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIdentityMapper {

    fun mapIcon(iconKey: String?): ImageVector {
        return when (iconKey) {
            "scissors" -> Icons.Default.ContentCut
            "medical" -> Icons.Default.MedicalServices
            "fitness" -> Icons.Default.FitnessCenter
            "face" -> Icons.Default.Face
            "spa" -> Icons.Default.SelfImprovement
            "home" -> Icons.Default.Home
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
