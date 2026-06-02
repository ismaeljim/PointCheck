package com.duoc.app.features.external.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

/**
 * AUDITORÍA: Servicio de Clima (Proxy OpenWeather).
 * Actúa como pasarela para evitar exponer la API Key en el cliente móvil.
 * Hallazgo: Se utiliza WebClient reactivo pero con llamada bloqueante (.block()).
 * Recomendación: Migrar a un flujo puramente reactivo si el tráfico escala para no agotar threads.
 */
@Service
class WeatherService(private val webClient: WebClient) {

    @Value("\${openweather.api.key}")
    private lateinit var apiKey: String

    @Value("\${openweather.api.base-url}")
    private lateinit var baseUrl: String

    @Value("\${openweather.api.path}")
    private lateinit var path: String

    fun getWeatherByCity(city: String): Any? =
        webClient.get()
            .uri { builder ->
                builder
                    .scheme("https")
                    .host("api.openweathermap.org")
                    .path(path)
                    .queryParam("q", city.trim())
                    .queryParam("appid", apiKey)
                    .queryParam("units", "metric")
                    .queryParam("lang", "es")
                    .build()
            }
            .retrieve()
            .bodyToMono(Any::class.java)
            .block()
}
