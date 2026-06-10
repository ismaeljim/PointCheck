package com.pointcheck.features.billing.data.dto

import com.pointcheck.core.data.dto.UserSummaryDto

/**
 * Objeto de transferencia de datos (DTO) que representa un registro de facturación o cobro.
 * Contiene el detalle del monto, las partes involucradas y el estado del pago.
 * 
 * @property id Identificador único del registro de facturación (UUID).
 * @property reservationId ID de la reserva asociada.
 * @property attentionId ID de la atención realizada (si aplica).
 * @property client Datos resumidos del cliente que debe pagar.
 * @property specialist Datos resumidos del especialista que recibe el pago.
 * @property amount Monto total a cobrar.
 * @property currency Moneda del cobro (ej: "CLP").
 * @property paymentMethod Método de pago utilizado (ej: "CASH", "TRANSFER").
 * @property status Estado del pago (ej: "PENDING", "PAID").
 * @property paidAt Fecha y hora en que se registró el pago.
 * @property externalReference Código de referencia externo (ej: número de transferencia).
 * @property notes Observaciones adicionales sobre el cobro.
 * @property createdAt Fecha de creación del registro.
 */
data class BillingRecordResponseDto(
    val id: String,
    val reservationId: String,
    val attentionId: String?,
    val client: UserSummaryDto,
    val specialist: UserSummaryDto,
    val amount: Double,
    val currency: String,
    val paymentMethod: String?,
    val status: String,
    val paidAt: String?,
    val externalReference: String?,
    val notes: String?,
    val createdAt: String
)
