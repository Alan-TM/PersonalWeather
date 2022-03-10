package mx.kodemia.personalweather.viewmodel

import android.app.Application
import android.app.Service
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.kodemia.personalweather.model.city.City
import mx.kodemia.personalweather.model.weather.WeatherEntity
import mx.kodemia.personalweather.network.service.ServiceNetwork
import mx.kodemia.personalweather.utils.checkForInternet
import java.lang.Exception

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val serviceNetwork = ServiceNetwork()
    private lateinit var sharedPreferences: SharedPreferences
    private val appid = "30ba6cd1ad33ea67e2dfd78a8d28ae62"

    val isLoading = MutableLiveData<Boolean>()
    val cityResponse = MutableLiveData<City>()
    val weatherResponse = MutableLiveData<WeatherEntity>()
    val error = MutableLiveData<String>()


    fun getCityAndWeather(latitude: String, longitude: String, units: String, lang: String) {
        isLoading.value = true
        viewModelScope.launch {
            try {
                getCityByLocation(latitude, longitude)
                getWeatherByLocation(latitude, longitude, units, lang)
            } catch(e: Exception){
                error.value = e.message
            }
            isLoading.value = false
        }
    }

    private suspend fun getCityByLocation(latitude: String, longitude: String) {
        val city = serviceNetwork.getCitiesByLatLon(latitude, longitude, appid)

        if (city.isSuccessful) {
            cityResponse.value = city.body()!!.first()
        } else {
            error.value = city.message()
        }
    }

    private suspend fun getWeatherByLocation(latitude: String, longitude: String, units: String, lang: String) {
        val weather = serviceNetwork.getWeatherByLatLon(latitude, longitude, units, lang, appid)

        if(weather.isSuccessful){
            weatherResponse.value = weather.body()
        } else {
            error.value = weather.message()
        }
    }
}

