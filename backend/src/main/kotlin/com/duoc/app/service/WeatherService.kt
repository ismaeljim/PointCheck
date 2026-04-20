package com.duoc.app.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

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
