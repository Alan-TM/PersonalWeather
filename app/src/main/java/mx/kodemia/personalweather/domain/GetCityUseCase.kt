package mx.kodemia.personalweather.domain

import mx.kodemia.personalweather.network.service.ServiceNetwork

class GetCityUseCase {

    private val repository = ServiceNetwork()

    suspend operator fun invoke(latitude: String, longitude: String) =
        repository.getCitiesByLatLon(latitude, longitude)
}