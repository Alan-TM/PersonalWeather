package mx.kodemia.personalweather.domain

import mx.kodemia.personalweather.data.ServiceNetwork
import javax.inject.Inject

class GetCityUseCase @Inject constructor(private val repository: ServiceNetwork) {

    suspend operator fun invoke(latitude: String, longitude: String) =
        repository.getCitiesByLatLon(latitude, longitude)
}