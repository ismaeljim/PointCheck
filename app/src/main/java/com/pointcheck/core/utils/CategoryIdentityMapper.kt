package com.pointcheck.core.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Mapeador encargado de transformar identificadores de identidad visual (iconos y colores)
 * provenientes del backend en componentes de UI de Jetpack Compose.
 *
 * Centraliza la lógica de "branding" dinámico para las categorías de servicio, asegurando
 * que la iconografía sea consistente entre la App y la configuración del sistema.
 */
object CategoryIdentityMapper {

    /**
     * Mapea una clave de icono en string a un [ImageVector] de Material Icons.
     *
     * @param iconKey Identificador del icono (ej: "scissors", "medical").
     * @return [ImageVector] correspondiente o un icono por defecto si no hay coincidencia.
     */
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

    /**
     * Convierte una cadena hexadecimal de color en un objeto [Color] de Compose.
     *
     * @param colorHex Código hexadecimal del color (ej: "#FF5733").
     * @return Objeto [Color] o un gris neutro por defecto si el formato es inválido.
     */
    fun mapColor(colorHex: String?): Color {
        return try {
            if (colorHex.isNullOrBlank()) Color(0xFF9E9E9E) // Gray default
            else Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            Color(0xFF9E9E9E)
        }
    }
}
