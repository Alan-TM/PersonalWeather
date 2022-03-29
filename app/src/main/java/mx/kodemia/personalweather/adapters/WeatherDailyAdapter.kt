package mx.kodemia.personalweather.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import mx.kodemia.personalweather.databinding.LayoutWeatherDailyBinding
import mx.kodemia.personalweather.model.weather_daily.WeatherDaily
import mx.kodemia.personalweather.utils.showIconHelper
import java.text.SimpleDateFormat
import java.util.*

class WeatherDailyAdapter(private val items: List<WeatherDaily>) :
    RecyclerView.Adapter<WeatherDailyAdapter.DailyViewHolder>() {
    class DailyViewHolder(private val binding: LayoutWeatherDailyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setInfo(item: WeatherDaily) {
            with(binding) {
                itemDayText.text = SimpleDateFormat("EEEE", Locale.ENGLISH).format(Date(item.dt * 1000))
                itemTemperature.text = item.temp.day.toInt().toString() + "º"
                itemStatus.load(showIconHelper(item.weather[0].icon))
                itemHumidityValue.text = "${item.humidity}%"
                itemWindValue.text = "${item.wind_speed} km/h"
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyViewHolder {
        val binding =
            LayoutWeatherDailyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DailyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyViewHolder, position: Int) {
        holder.setInfo(items[position])
    }

    override fun getItemCount(): Int = items.size
}