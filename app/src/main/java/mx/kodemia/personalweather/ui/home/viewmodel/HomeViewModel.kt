package mx.kodemia.personalweather.ui.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.kodemia.personalweather.core.utils.capitalizeText
import mx.kodemia.personalweather.core.utils.dayParser
import mx.kodemia.personalweather.core.utils.hourParser
import mx.kodemia.personalweather.domain.GetCityUseCase
import mx.kodemia.personalweather.domain.GetWeatherUseCase
import mx.kodemia.personalweather.data.model.city.City
import mx.kodemia.personalweather.data.model.weather.WeatherEntity

class HomeViewModel : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _cityResponse = MutableLiveData<City>()
    val cityResponse: LiveData<City> = _cityResponse
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    private val _weatherDaily = MutableLiveData<ArrayList<HashMap<String, String>>>()
    val weatherDaily: LiveData<ArrayList<HashMap<String, String>>> = _weatherDaily
    private val _unitSymbol = MutableLiveData<String>()
    private val _dataForView = MutableLiveData<HashMap<String, String>>()
    val dataForView: LiveData<HashMap<String, String>> = _dataForView

    private val getCityUseCase = GetCityUseCase()
    private val getWeatherUseCase = GetWeatherUseCase()
    private lateinit var preferencesCall: HashMap<String, String>

    fun getCityAndWeather() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                preferencesCall.let {
                    getCityByLocation(it["lat"]!!, it["lon"]!!)
                    getWeatherByLocation(it["lat"]!!, it["lon"]!!, it["units"]!!, it["lang"]!!)
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
            _isLoading.value = false
        }
    }

    private suspend fun getCityByLocation(latitude: String, longitude: String) {
        val city = getCityUseCase(latitude, longitude)

        if (city.isSuccessful) {
            _cityResponse.value = city.body()!!.first()
        } else {
            _error.value = city.message()
        }
    }

    private suspend fun getWeatherByLocation(
        latitude: String,
        longitude: String,
        units: String,
        lang: String
    ) {
        val weather = getWeatherUseCase(latitude, longitude, units, lang)

        if (weather.isSuccessful) {
            weather.body()?.let {
                setDataForView(it)
                _weatherDaily.value = dailyWeather(it)
            }
        } else {
            _error.value = weather.message()
        }
    }

    private fun dailyWeather(weather: WeatherEntity): ArrayList<HashMap<String, String>> {
        val dummyList = ArrayList<HashMap<String, String>>()
        for (i in 1 until weather.daily.size) {
            dummyList.add(
                hashMapOf(
                    Pair(
                        "temperature",
                        "${weather.daily[i].temp.day.toInt()} ${_unitSymbol.value}"
                    ),
                    Pair("day", dayParser(weather.daily[i].dt)),
                    Pair("icon", weather.daily[i].weather[0].icon),
                    Pair("humidity", "${weather.daily[i].humidity} %"),
                    Pair("wind", "${weather.daily[i].wind_speed} km/h")
                )
            )
        }
        return dummyList
    }

    fun setDataForAPICall(latitude: String, longitude: String, units: Boolean, lang: Boolean) {
        var unit = "metric"
        var languageCode = "es"

        if (units) {
            unit = "imperial"
            _unitSymbol.value = "ºF"
        } else {
            _unitSymbol.value = "ºC"
        }
        if (lang) {
            languageCode = "en"
        }

        preferencesCall = hashMapOf(
            Pair("lat", latitude),
            Pair("lon", longitude),
            Pair("units", unit),
            Pair("lang", languageCode)
        )
    }

    private fun setDataForView(weather: WeatherEntity) {
        val temp = "${weather.current.temp.toInt()} ${_unitSymbol.value}"
        val updatedAt = hourParser(weather.current.dt)
        val status = capitalizeText(weather.current.weather[0].description)
        val sunrise = hourParser(weather.current.sunrise)
        val sunset = hourParser(weather.current.sunset)
        val wind = weather.current.wind_speed.toString()
        val pressure = weather.current.pressure.toString()
        val humidity = "${weather.current.humidity} %"
        val feelsLike = "${weather.current.feels_like.toInt()} ${_unitSymbol.value}"
        val icon = weather.current.weather[0].icon

        _dataForView.value = hashMapOf(
            Pair("temperature", temp),
            Pair("updatedAt", updatedAt),
            Pair("status", status),
            Pair("sunrise", sunrise),
            Pair("sunset", sunset),
            Pair("wind", wind),
            Pair("pressure", pressure),
            Pair("humidity", humidity),
            Pair("feelsLike", feelsLike),
            Pair("icon", icon),
        )
    }
}

