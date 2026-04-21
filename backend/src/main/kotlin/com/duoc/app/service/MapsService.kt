package com.duoc.app.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class MapsService(private val webClient: WebClient) {

    @Value("\${google.maps.api.key}")
    private lateinit var apiKey: String

    @Value("\${google.maps.place.details.url}")
    private lateinit var placeUrl: String

    fun getPlaceDetails(placeId: String): Any? =
        webClient.get()
            .uri { builder ->
                builder.path(placeUrl)
                    .queryParam("place_id", placeId)
                    .queryParam("key", apiKey)
                    .build()
            }
            .retrieve()
            .bodyToMono(Any::class.java)
            .block()
}
