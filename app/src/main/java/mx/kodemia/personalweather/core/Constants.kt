package mx.kodemia.personalweather.core

object Constants {
    const val BASE_URL = "https://api.openweathermap.org/"
    const val API_WEATHER_END_POINT = "data/2.5/onecall"
    const val API_CITY_END_POINT = "geo/1.0/reverse"
    const val API_KEY = "804c1de62c103c56406e6fe94bf50b86"
    const val EXCLUDE_PARAMETERS = "minutely,hourly,alerts"

    const val REQUEST_PERMISSIONS_REQUEST_CODE = 34

    const val ERROR_NO_INTERNET = 999
    const val ERROR_IO = 900
    const val ERROR_UNAUTHORIZED = 401
    const val ERROR_NOT_FOUND = 404
}