package mx.kodemia.personalweather.data.network.api

import mx.kodemia.personalweather.core.Constants.API_CITY_END_POINT
import mx.kodemia.personalweather.core.Constants.API_WEATHER_END_POINT
import mx.kodemia.personalweather.core.Constants.EXCLUDE_PARAMETERS
import mx.kodemia.personalweather.data.model.city.City
import mx.kodemia.personalweather.data.model.weather.WeatherEntity
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface WeatherService {
    @GET(API_WEATHER_END_POINT)
    suspend fun getWeatherById(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("units") units: String?,
        @Query("lang") lang: String?,
        @Query("appid") appid: String,
        @Query("exclude") exclude: String = EXCLUDE_PARAMETERS
    ): Response<WeatherEntity>

    @GET(API_CITY_END_POINT)
    suspend fun getCitiesByLatLon(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") appid: String
    ): Response<List<City>>
}