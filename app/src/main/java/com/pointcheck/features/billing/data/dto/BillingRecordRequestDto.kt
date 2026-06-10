package com.pointcheck.features.billing.data.dto

/**
 * Objeto de transferencia de datos (DTO) para la creación de un nuevo registro de facturación.
 * 
 * @property reservationId Identificador de la reserva vinculada al cobro.
 * @property attentionId Identificador de la atención técnica realizada (opcional).
 * @property amount Monto total a cobrar por el servicio.
 * @property currency Moneda en la que se expresa el monto (por defecto "CLP").
 * @property paymentMethod Método de pago sugerido o seleccionado inicialmente.
 * @property notes Observaciones o detalles adicionales sobre el cobro.
 */
data class BillingRecordRequestDto(
    val reservationId: String,
    val attentionId: String? = null,
    val amount: Double,
    val currency: String = "CLP",
    val paymentMethod: String? = null,
    val notes: String? = null
)
