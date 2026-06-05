package com.duoc.app.core.util

/**
 * Utilidades para la gestión de RUT chileno.
 * Versión Senior: Flexible con el formato de entrada, estricta con la validez.
 */
object RutUtils {

    /**
     * Limpia el RUT de cualquier caracter no alfanumérico y asegura formato 12345678-9.
     */
    fun formatRut(rut: String): String {
        // Elimina todo lo que no sea número o K
        val clean = rut.replace(Regex("[^0-9kK]"), "").uppercase()
        
        if (clean.length < 2) return clean
        
        val body = clean.substring(0, clean.length - 1)
        val dv = clean.substring(clean.length - 1)
        return "$body-$dv"
    }

    /**
     * Valida un RUT chileno sin importar el formato original (puntos, guiones, espacios).
     */
    fun validateRut(rut: String): Boolean {
        try {
            // Limpieza profunda para validación
            val clean = rut.replace(Regex("[^0-9kK]"), "").uppercase()
            
            if (clean.length < 2 || clean.length > 10) return false
            
            val body = clean.substring(0, clean.length - 1)
            val dv = clean.substring(clean.length - 1)
            
            // Validamos que el cuerpo sea numérico
            if (!body.all { it.isDigit() }) return false
            
            return calculateDV(body) == dv
        } catch (e: Exception) {
            return false
        }
    }

    private fun calculateDV(rutBody: String): String {
        var sum = 0
        var multiplier = 2
        
        for (i in rutBody.length - 1 downTo 0) {
            sum += Character.getNumericValue(rutBody[i]) * multiplier
            multiplier = if (multiplier == 7) 2 else multiplier + 1
        }
        
        val expectedDv = 11 - (sum % 11)
        return when (expectedDv) {
            11 -> "0"
            10 -> "K"
            else -> expectedDv.toString()
        }
    }
}
