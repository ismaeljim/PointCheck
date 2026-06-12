package com.pointcheck.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * PointCheck Design System - Shapes
 * Defined for consistency across components and containers.
 */
val AppShapes = Shapes(
    // Componentes estándar: Botones, Inputs (8dp)
    small = RoundedCornerShape(8.dp),
    
    // Contenedores: Tarjetas, Secciones principales (16dp)
    medium = RoundedCornerShape(16.dp),
    
    // Contenedores grandes: Diálogos, Bottom Sheets (24dp)
    large = RoundedCornerShape(24.dp)
)
