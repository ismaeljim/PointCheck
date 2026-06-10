package com.pointcheck.features.reservation.data.dto

/**
 * Objeto de transferencia de datos (DTO) para la creación de una nueva reserva.
 * 
 * @property clientId Identificador del cliente que solicita el servicio.
 * @property specialistId Identificador del especialista que prestará el servicio.
 * @property serviceId Identificador del servicio específico solicitado.
 * @property reservationStart Fecha y hora de inicio deseada.
 * @property reservationEnd Fecha y hora de término estimada (opcional).
 * @property notes Instrucciones o requerimientos especiales del cliente.
 * @property paymentMethod Método de pago preferido.
 */
data class ReservationRequestDto(
    val clientId: String,
    val specialistId: String,
    val serviceId: String? = null,
    val reservationStart: String,
    val reservationEnd: String? = null,
    val notes: String? = null,
    val paymentMethod: String? = null
)
