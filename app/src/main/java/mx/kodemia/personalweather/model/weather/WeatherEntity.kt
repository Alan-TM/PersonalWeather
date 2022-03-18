package mx.kodemia.personalweather.model.weather

import mx.kodemia.personalweather.model.weather_daily.WeatherDaily

data class WeatherEntity(
    val current: Current,
    val daily: List<WeatherDaily>
)

/*
val base: String,
val main: Main,
val sys: Sys,
val id: Int,
val name: String,
val wind: Wind,
val weather: List<Weather>,
val dt: Long
*/
