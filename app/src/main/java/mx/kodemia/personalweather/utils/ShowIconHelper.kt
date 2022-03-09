package mx.kodemia.personalweather.utils

import mx.kodemia.personalweather.R

fun showIconHelper(apiIcon: String): Int{
    return when(apiIcon){
        "01d" -> R.drawable.clear_day
        "01n" -> R.drawable.clear_night
        "02d" -> R.drawable.partly_cloudy_day
        "02n" -> R.drawable.partly_cloudy_night
        "03d", "03n" -> R.drawable.cloudy
        "04d", "04n" -> R.drawable.overcast
        "09d", "09n" -> R.drawable.heavy_showers
        "10d", "10n" -> R.drawable.showers
        "11d", "11n" -> R.drawable.thunderstorm_showers
        else -> R.drawable.clear_day
    }
}