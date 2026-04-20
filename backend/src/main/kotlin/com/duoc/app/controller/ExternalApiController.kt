package com.duoc.app.controller

import com.duoc.app.service.MapsService
import com.duoc.app.service.WeatherService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/external")
@CrossOrigin("*")
class ExternalApiController(
    private val weatherService: WeatherService,
    private val mapsService: MapsService
) {

    @GetMapping("/weather/{city}")
    fun getWeather(@PathVariable city: String): ResponseEntity<Any> {
        val weatherData = weatherService.getWeatherByCity(city)
        return ResponseEntity.ok(weatherData)
    }

    @GetMapping("/place/{placeId}")
    fun getPlace(@PathVariable placeId: String): ResponseEntity<Any> {
        val placeData = mapsService.getPlaceDetails(placeId)
        return ResponseEntity.ok(placeData)
    }
}
