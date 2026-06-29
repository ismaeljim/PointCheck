package com.pointcheck.core.utils

import java.text.NumberFormat
import java.util.Locale

object FormatUtils {
    /**
     * Formatea un valor numérico a moneda Chilena (CLP).
     * Ejemplo: 10000 -> $10.000
     */
    fun formatCurrency(amount: Double?): String {
        if (amount == null) return "$0"
        val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
        format.minimumFractionDigits = 0
        format.maximumFractionDigits = 0
        return format.format(amount).replace("CLP", "").trim()
    }

    /**
     * Formatea un valor numérico a moneda Chilena (CLP) sin el símbolo $.
     */
    fun formatNumber(amount: Double?): String {
        if (amount == null) return "0"
        val format = NumberFormat.getInstance(Locale("es", "CL"))
        format.minimumFractionDigits = 0
        format.maximumFractionDigits = 0
        return format.format(amount)
    }
}
