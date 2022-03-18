package mx.kodemia.personalweather.model.city

import mx.kodemia.personalweather.model.weather.Current

data class CityEntity(
    val current: Current,
    var city: City?
)
