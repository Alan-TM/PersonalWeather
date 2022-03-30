package mx.kodemia.personalweather.domain

import mx.kodemia.personalweather.data.ServiceNetwork

class GetWeatherUseCase {
    private val repository = ServiceNetwork()

    suspend operator fun invoke(
        latitude: String,
        longitude: String,
        units: String,
        lang: String
    ) = repository.getWeatherByLatLon(latitude, longitude, units, lang)
}