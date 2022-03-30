package mx.kodemia.personalweather.data.model.city

import mx.kodemia.personalweather.data.model.weather.Current

data class CityEntity(
    val current: Current,
    var city: City?
)
