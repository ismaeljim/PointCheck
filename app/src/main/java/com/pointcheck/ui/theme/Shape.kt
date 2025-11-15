package com.pointcheck.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Define las formas de los componentes de la app
val Shapes = Shapes(
    // Esquinas ligeramente redondeadas para componentes pequeños
    small = RoundedCornerShape(8.dp),
    // Redondeo medio para botones y tarjetas
    medium = RoundedCornerShape(12.dp),
    // Redondeo grande para componentes que lo requieran
    large = RoundedCornerShape(16.dp)
)
