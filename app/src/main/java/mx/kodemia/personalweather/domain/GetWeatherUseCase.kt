package mx.kodemia.personalweather.domain

import mx.kodemia.personalweather.data.ServiceNetwork
import javax.inject.Inject

class GetWeatherUseCase @Inject constructor(private val repository: ServiceNetwork) {

    suspend operator fun invoke(
        latitude: String,
        longitude: String,
        units: String,
        lang: String
    ) = repository.getWeatherByLatLon(latitude, longitude, units, lang)
}