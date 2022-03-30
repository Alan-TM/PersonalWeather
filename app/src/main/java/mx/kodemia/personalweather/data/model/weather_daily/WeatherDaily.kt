package mx.kodemia.personalweather.data.model.weather_daily

import mx.kodemia.personalweather.data.model.weather.Weather

data class WeatherDaily(
    val dt: Long,
    val temp: DailyTemp,
    val humidity: Int,
    val wind_speed: Double,
    val weather: List<Weather>
)
