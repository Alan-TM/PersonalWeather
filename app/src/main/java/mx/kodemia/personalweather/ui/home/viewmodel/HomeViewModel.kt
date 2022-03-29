package mx.kodemia.personalweather.ui.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.kodemia.personalweather.core.Constants.API_KEY
import mx.kodemia.personalweather.model.city.City
import mx.kodemia.personalweather.model.weather.WeatherEntity
import mx.kodemia.personalweather.model.weather_daily.WeatherDaily
import mx.kodemia.personalweather.network.service.ServiceNetwork
import java.lang.Exception

class HomeViewModel() : ViewModel() {
    private val serviceNetwork = ServiceNetwork()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _cityResponse = MutableLiveData<City>()
    val cityResponse: LiveData<City> = _cityResponse
    private val _weatherResponse = MutableLiveData<WeatherEntity>()
    val weatherResponse: LiveData<WeatherEntity> = _weatherResponse
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    private val _weatherDaily = MutableLiveData<List<WeatherDaily>>()
    val weatherDaily: LiveData<List<WeatherDaily>> = _weatherDaily

    private lateinit var preferencesCall: HashMap<String, String>


    fun getCityAndWeather() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                preferencesCall.let {
                    getCityByLocation(it["lat"]!!, it["lon"]!!)
                    getWeatherByLocation(it["lat"]!!, it["lon"]!!, it["units"]!!, it["lang"]!!)
                }
            } catch(e: Exception){
                _error.value = e.message
            }
            _isLoading.value = false
        }
    }

    private suspend fun getCityByLocation(latitude: String, longitude: String) {
        val city = serviceNetwork.getCitiesByLatLon(latitude, longitude, API_KEY)

        if (city.isSuccessful) {
            _cityResponse.value = city.body()!!.first()
        } else {
            _error.value = city.message()
        }
    }

    private suspend fun getWeatherByLocation(latitude: String, longitude: String, units: String, lang: String) {
        val weather = serviceNetwork.getWeatherByLatLon(latitude, longitude, units, lang, API_KEY)

        if(weather.isSuccessful){
            _weatherResponse.value = weather.body()
            _weatherDaily.value = weather.body()?.let { dailyWeather(it) }
        } else {
            _error.value = weather.message()
        }
    }

    private fun dailyWeather(weather: WeatherEntity): List<WeatherDaily>{
        val dummyList = mutableListOf<WeatherDaily>()
        for(i in 1 until weather.daily.size){
            dummyList.add(weather.daily[i])
        }
        return dummyList
    }

    fun setDataForAPICall(latitude: String, longitude: String, units: Boolean, lang: Boolean){
        var unit = "metric"
        var languageCode = "es"

        if (units) {
            unit = "imperial"
        }
        if (lang) {
            languageCode = "en"
        }
        preferencesCall = hashMapOf(Pair("lat", latitude), Pair("lon", longitude), Pair("units", unit), Pair("lang", languageCode))
    }
}

