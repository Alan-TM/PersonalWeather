package mx.kodemia.personalweather.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.kodemia.personalweather.core.Constants.API_KEY
import mx.kodemia.personalweather.data.model.city.City
import mx.kodemia.personalweather.data.model.weather.WeatherEntity
import mx.kodemia.personalweather.data.network.api.WeatherService
import retrofit2.Response
import javax.inject.Inject

class ServiceNetwork @Inject constructor(private val api: WeatherService) {

    suspend fun getCitiesByLatLon(latitude: String, longitude: String): Response<List<City>> =
        withContext(Dispatchers.IO) {
            api.getCitiesByLatLon(latitude, longitude, API_KEY)
        }

    suspend fun getWeatherByLatLon(
        latitude: String,
        longitude: String,
        units: String,
        lang: String
    ): Response<WeatherEntity> =
        withContext(Dispatchers.IO) {
            api.getWeatherById(latitude, longitude, units, lang, API_KEY)
        }
}