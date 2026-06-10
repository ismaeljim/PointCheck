package com.pointcheck.features.billing.data.dto

/**
 * Objeto de transferencia de datos (DTO) para la solicitud de marcar un cobro como pagado.
 * 
 * @property paymentMethod Medio por el cual se realizó el pago (ej: "CASH", "TRANSFER", "DEBIT").
 * @property externalReference Código o número de comprobante externo si existe.
 * @property notes Observaciones adicionales sobre la transacción de pago.
 */
data class MarkAsPaidRequestDto(
    val paymentMethod: String,
    val externalReference: String? = null,
    val notes: String? = null
)
