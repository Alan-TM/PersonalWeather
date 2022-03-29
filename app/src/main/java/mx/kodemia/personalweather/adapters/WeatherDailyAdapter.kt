package mx.kodemia.personalweather.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import mx.kodemia.personalweather.databinding.LayoutWeatherDailyBinding
import mx.kodemia.personalweather.utils.showIconHelper

class WeatherDailyAdapter(private val items: ArrayList<HashMap<String, String>>) :
    RecyclerView.Adapter<WeatherDailyAdapter.DailyViewHolder>() {
    class DailyViewHolder(private val binding: LayoutWeatherDailyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setInfo(item: HashMap<String, String>) {
            with(binding) {
                itemDayText.text = item["day"]
                itemTemperature.text = item["temperature"]!!
                itemStatus.load(showIconHelper(item["icon"]!!))
                itemHumidityValue.text = item["humidity"]
                itemWindValue.text = item["wind"]
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