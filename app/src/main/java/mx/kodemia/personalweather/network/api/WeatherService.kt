package mx.kodemia.personalweather.network.api

import mx.kodemia.personalweather.model.city.City
import mx.kodemia.personalweather.model.weather.WeatherEntity
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface WeatherService {
    //@GET("data/2.5/weather")
    // api.openweathermap.org/data/2.5/weather?lat=35&lon=139&appid={API key}
    @GET("data/2.5/onecall")
    // api.openweathermap.org/data/2.5/onecall?lat={lat}&lon={lon}&exclude={part}&appid={API key}
    suspend fun getWeatherById(
        //@Query("id") lon: Long,
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("units") units: String?,
        @Query("lang") lang: String?,  // Para el idioma
        @Query("appid") appid: String,
        @Query("exclude") exclude: String = "minutely,hourly,alerts"
    ): Response<WeatherEntity>

    @GET("geo/1.0/reverse")
    suspend fun getCitiesByLatLon(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") appid: String
    ): Response<List<City>>
}