package mx.kodemia.personalweather.model.city

import mx.kodemia.personalweather.model.Current

data class CityEntity(
    val current: Current,
    var city: City?
)
