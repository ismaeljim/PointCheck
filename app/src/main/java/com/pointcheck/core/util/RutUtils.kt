package com.pointcheck.core.util

/**
 * Objeto de utilidad para el manejo y validación de RUT (Rol Único Tributario) chileno.
 *
 * Provee métodos para limpiar, formatear y validar cadenas de RUT según el algoritmo
 * de módulo 11. Está diseñado para ser flexible, aceptando varios formatos de entrada
 * y normalizándolos para la comunicación con el servidor o la visualización en la UI.
 */
object RutUtils {

    /**
     * Normaliza una cadena de RUT al formato estándar "cuerpo-dv" (ej: 12345678-K).
     *
     * @param rut La cadena de RUT original a ser formateada.
     * @return Una cadena formateada apta para la transmisión vía API.
     */
    fun formatForServer(rut: String): String {
        val clean = rut.replace(Regex("[^0-9kK]"), "").uppercase()
        if (clean.length < 2) return clean
        val body = clean.substring(0, clean.length - 1)
        val dv = clean.substring(clean.length - 1)
        return "$body-$dv"
    }

    /**
     * Valida la integridad lógica de un RUT utilizando el algoritmo de módulo 11.
     *
     * @param rut La cadena de RUT a validar (puede incluir puntos, guiones o espacios).
     * @return `true` si el RUT es válido, `false` en caso contrario.
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

    /**
     * Calcula el dígito verificador para un cuerpo de RUT dado.
     * 
     * @param rutBody El cuerpo numérico del RUT.
     * @return El dígito verificador calculado (0-9 o K).
     */
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
