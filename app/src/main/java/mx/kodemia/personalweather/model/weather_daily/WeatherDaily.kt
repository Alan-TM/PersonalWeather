package mx.kodemia.personalweather.model.weather_daily

import mx.kodemia.personalweather.model.weather.Weather

data class WeatherDaily(
    val dt: Long,
    val temp: DailyTemp,
    val humidity: Int,
    val wind_speed: Double,
    val weather: List<Weather>
)
