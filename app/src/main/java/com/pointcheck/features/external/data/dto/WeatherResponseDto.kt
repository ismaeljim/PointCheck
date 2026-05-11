package com.pointcheck.features.external.data.dto

import com.google.gson.annotations.SerializedName

data class WeatherResponseDto(
    val main: MainDto,
    val weather: List<WeatherDescriptionDto>,
    val name: String
)

data class MainDto(
    val temp: Double,
    val humidity: Int
)

data class WeatherDescriptionDto(
    val description: String,
    val icon: String
)
