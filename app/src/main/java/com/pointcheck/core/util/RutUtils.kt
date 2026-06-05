package com.pointcheck.core.util

/**
 * Utilidades para la gestión de RUT chileno en Android.
 * Versión Flexible: Permite cualquier entrada y valida la lógica.
 */
object RutUtils {

    /**
     * Limpia el RUT para enviarlo al servidor en formato estándar 12345678-9.
     */
    fun formatForServer(rut: String): String {
        val clean = rut.replace(Regex("[^0-9kK]"), "").uppercase()
        if (clean.length < 2) return clean
        val body = clean.substring(0, clean.length - 1)
        val dv = clean.substring(clean.length - 1)
        return "$body-$dv"
    }

    /**
     * Valida la lógica del RUT sin importar el formato.
     */
    fun validateRut(rut: String): Boolean {
        try {
            val clean = rut.replace(Regex("[^0-9kK]"), "").uppercase()
            if (clean.length < 2 || clean.length > 10) return false
            
            val body = clean.substring(0, clean.length - 1)
            val dv = clean.substring(clean.length - 1)
            
            return calculateDV(body) == dv
        } catch (e: Exception) {
            return false
        }
    }

    private fun calculateDV(rutBody: String): String {
        var sum = 0
        var multiplier = 2
        for (i in rutBody.length - 1 downTo 0) {
            val char = rutBody[i]
            if (!char.isDigit()) return ""
            sum += Character.getNumericValue(char) * multiplier
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
