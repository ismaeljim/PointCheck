package com.duoc.app.core.util

object RutUtils {

    /**
     * Limpia el RUT eliminando puntos, espacios y convirtiendo a mayúsculas.
     * Retorna el RUT en formato 12345678-9
     */
    fun formatRut(rut: String): String {
        val clean = rut.replace(".", "").replace(" ", "").uppercase()
        if (!clean.contains("-")) {
            if (clean.length < 2) return clean
            val body = clean.substring(0, clean.length - 1)
            val dv = clean.substring(clean.length - 1)
            return "$body-$dv"
        }
        return clean
    }

    /**
     * Valida un RUT chileno usando el algoritmo de Módulo 11.
     */
    fun validateRut(rut: String): Boolean {
        try {
            val cleanRut = rut.replace(".", "").replace(" ", "").uppercase()
            if (!cleanRut.matches(Regex("^[0-9]+-[0-9K]$"))) return false
            
            val parts = cleanRut.split("-")
            if (parts.size != 2) return false
            
            val body = parts[0]
            val dv = parts[1]
            
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
