package mx.kodemia.personalweather.network.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.kodemia.personalweather.model.city.City
import mx.kodemia.personalweather.model.weather.WeatherEntity
import mx.kodemia.personalweather.network.api.RetrofitInstance
import mx.kodemia.personalweather.network.api.WeatherService
import retrofit2.Response
import retrofit2.create

class ServiceNetwork {
    private val retrofit = RetrofitInstance.getInstance().create(WeatherService::class.java)

    suspend fun getCitiesByLatLon(latitude: String, longitude: String, appid: String): Response<List<City>> =
        withContext(Dispatchers.IO){
            retrofit.getCitiesByLatLon(latitude, longitude, appid)
        }

    suspend fun getWeatherByLatLon(latitude: String, longitude: String, units: String, lang: String, appid: String): Response<WeatherEntity> =
        withContext(Dispatchers.IO){
            retrofit.getWeatherById(latitude, longitude, units, lang, appid)
        }
}