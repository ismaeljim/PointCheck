package com.pointcheck.core.util

object RutUtils {
    fun formatRut(rut: String): String {
        val clean = rut.replace(".", "").replace("-", "").replace(" ", "").uppercase()
        if (clean.isEmpty()) return ""
        val dv = clean.takeLast(1)
        val body = clean.dropLast(1)
        if (body.isEmpty()) return dv
        
        val formattedBody = body.reversed().chunked(3).joinToString(".").reversed()
        return "$formattedBody-$dv"
    }

    fun validateRut(rut: String): Boolean {
        val cleanRut = rut.replace(".", "").replace("-", "").replace(" ", "").uppercase()
        if (cleanRut.length < 2) return false
        
        val body = cleanRut.dropLast(1)
        val dv = cleanRut.takeLast(1)
        
        return calculateDV(body) == dv
    }

    private fun calculateDV(rutBody: String): String {
        var sum = 0
        var multiplier = 2
        
        for (i in rutBody.length - 1 downTo 0) {
            val digit = rutBody[i].digitToIntOrNull() ?: return ""
            sum += digit * multiplier
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
