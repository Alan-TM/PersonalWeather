package mx.kodemia.personalweather.data.model.weather

import mx.kodemia.personalweather.data.model.weather_daily.WeatherDaily

data class WeatherEntity(
    val current: Current,
    val daily: List<WeatherDaily>
)