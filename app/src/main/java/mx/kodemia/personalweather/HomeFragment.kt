package mx.kodemia.personalweather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentTabHost
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.kodemia.personalweather.BuildConfig.APPLICATION_ID
import mx.kodemia.personalweather.databinding.FragmentHomeBinding
import mx.kodemia.personalweather.model.city.City
import mx.kodemia.personalweather.model.weather.WeatherEntity
import mx.kodemia.personalweather.network.service.WeatherService
import mx.kodemia.personalweather.utils.checkForInternet
import mx.kodemia.personalweather.utils.showIconHelper
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val TAG = "MainActivityError"
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34

    private var latitude = ""
    private var longitude = ""

    private var units = false
    private var language = false

    /**
     * Punto de entrada para el API Fused Location Provider.
     */
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private fun onRefreshAPICall() {
        with(binding.swipeRefreshRootLayout) {
            setOnRefreshListener {
                //showError("Actualizado")
                this.isRefreshing = false
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        if (!checkPermissions()) {
            requestPermissions()
        } else {
            // despues de que se obtiene la location se ejecuta el setUpViewData con esa location
            getLastLocation() { location ->
                setupViewData(location)
            }
        }

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        units = sharedPreferences.getBoolean("units", false)
        language = sharedPreferences.getBoolean("language", false)

        onRefreshAPICall()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun setupViewData(location: Location) {

        if (checkForInternet(requireContext())) {
            // Se coloca en este punto para permitir su ejecución
            showIndicator(true)
            lifecycleScope.launch {
                latitude = location.latitude.toString()
                longitude = location.longitude.toString()
                formatResponse(getWeather(), getCitiesByLatLon())
            }
        } else {
            showError(getString(R.string.no_internet_access))
            binding.detailsContainer.isVisible = false
        }
    }

    private suspend fun getWeather(): WeatherEntity = withContext(Dispatchers.IO) {
        Log.e(TAG, "CORR Lat: $latitude Long: $longitude")
        var unit = "metric"
        var languageCode = "es"

        if (units) {
            unit = "imperial"
        }
        if (language) {
            languageCode = "en"
        }
        // showIndicator(true)
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service: WeatherService = retrofit.create(WeatherService::class.java)

        service.getWeatherById(
            latitude,
            longitude,
            unit,
            languageCode,
            "30ba6cd1ad33ea67e2dfd78a8d28ae62"
        )
    }

    suspend fun getCitiesByLatLon(): City =
        withContext(Dispatchers.IO) {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service: WeatherService = retrofit.create(WeatherService::class.java)

            service.getCitiesByLatLon(latitude, longitude, "88d676ed50920fa123c15777f87f3955").first()
        }

    /**
     * Función para mostrar los datos obtenidos de OpenWeather
     */

    private fun formatResponse(weatherEntity: WeatherEntity, city: City) {
        var unitSymbol = "ºC"

        if (units) {
            unitSymbol = "ºF"
        }

        try {
            val temp = "${weatherEntity.current.temp.toInt()}$unitSymbol"
            val cityName = city.name //weatherEntity.name
            val country = city.country //weatherEntity.sys.country
            val address = "$cityName, $country"
            val tempMin = "" //"Mín: ${weatherEntity.main.temp_min.toInt()}º"
            val tempMax = "" //""Max: ${weatherEntity.main.temp_max.toInt()}º"
            // Capitalizar la primera letra de la descripción
            var status = ""
            val weatherDescription = weatherEntity.current.weather[0].description
            if (weatherDescription.isNotEmpty()) {
                status = (weatherDescription[0].uppercaseChar() + weatherDescription.substring(1))
            }
            val dt = weatherEntity.current.dt
            val updatedAt = getString(R.string.updatedAt) + SimpleDateFormat(
                "hh:mm a",
                Locale.ENGLISH
            ).format(Date(dt * 1000))
            val sunrise = weatherEntity.current.sunrise
            val sunriseFormat =
                SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise * 1000))
            val sunset = weatherEntity.current.sunset
            val sunsetFormat =
                SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset * 1000))
            val wind = "${weatherEntity.current.wind_speed} km/h"
            val pressure = "${weatherEntity.current.pressure} mb"
            val humidity = "${weatherEntity.current.humidity}%"
            val feelsLike =
                getString(R.string.sensation) + weatherEntity.current.feels_like.toInt() + unitSymbol
            val icon = weatherEntity.current.weather[0].icon
            val iconUrl = "https://openweathermap.org/img/w/$icon.png"

            binding.apply {
                iconImageView.load(showIconHelper(icon))
                addressTextView.text = address
                dateTextView.text = updatedAt
                temperatureTextView.text = temp
                statusTextView.text = status
                tempMinTextView.text = tempMin
                tempMaxTextView.text = tempMax
                sunriseTextView.text = sunriseFormat
                sunsetTextView.text = sunsetFormat
                windTextView.text = wind
                pressureTextView.text = pressure
                humidityTextView.text = humidity
                detailsContainer.isVisible = true
                feelsLikeTextView.text = feelsLike
            }

            applyAnimations()
            showIndicator(false)
        } catch (exception: Exception) {
            showError(getString(R.string.error_ocurred))
            Log.e("Error format", "Ha ocurrido un error")
            showIndicator(false)
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun showIndicator(visible: Boolean) {
        binding.progressBarIndicator.isVisible = visible
    }

    private fun applyAnimations() {
        val cvAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.headline_card_view_fade_in)
        binding.headlineCardView.animation = cvAnimation

        val detailsSlideRightAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.details_slide_in)
        binding.detailsCardView.animation = detailsSlideRightAnimation
        binding.tempMinTextView.animation = detailsSlideRightAnimation

        val detailsSlideLeftAnimation = AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.details_slide_left
        )
        binding.tempMaxTextView.animation = detailsSlideLeftAnimation
        binding.dateTextView.animation = detailsSlideLeftAnimation
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation(onLocation: (location: Location) -> Unit) {
        Log.d(TAG, "Aquí estoy: $latitude Long: $longitude")
        fusedLocationClient.lastLocation
            .addOnCompleteListener { taskLocation ->
                if (taskLocation.isSuccessful && taskLocation.result != null) {

                    val location = taskLocation.result

                    latitude = location?.latitude.toString()
                    longitude = location?.longitude.toString()
                    Log.d(TAG, "GetLasLoc Lat: $latitude Long: $longitude")

                    onLocation(taskLocation.result)
                } else {
                    Log.w(TAG, "getLastLocation:exception", taskLocation.exception)
                    showSnackbar(R.string.no_location_detected)
                }
            }
    }

    /**
     * Devuelve el estado de los permisos que se necesitan
     */

    private fun checkPermissions() =
        ActivityCompat.checkSelfPermission(requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    private fun requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            )) {
            // Proporciona una explicación adicional al usuario (rationale). Esto ocurre si el usuario
            // niega el permiso previamente pero no marca la casilla "No volver a preguntar".
            Log.i(
                TAG,
                "Muestra explicación rationale para proveer una contexto adicional de porque se requiere el permiso"
            )
            showSnackbar(R.string.permission_rationale, android.R.string.ok) {
                // Solicitar permiso
                startLocationPermissionRequest()
            }

        } else {
            // Solicitar permiso. Es posible que esto pueda ser contestado de forma automática
            // si la configuración del dispositivo define el permiso a un estado predefinido o
            // si el usuario anteriormente activo "No presenter de nuevo".
            Log.i(TAG, "Solicitando permiso")
            startLocationPermissionRequest()
        }
    }

    /**
     * Callback recibido cuando se ha completado una solicitud de permiso.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                // Si el flujo es interrumpido, la solicitud de permiso es cancelada y se
                // reciben arrays vacios.
                grantResults.isEmpty() -> Log.i(TAG, "La interacción del usuario fue cancelada.")

                // Permiso otorgado.
                // Podemos pasar la referencia a una funcion si cumple con el mismo prototipo
                (grantResults[0] == PackageManager.PERMISSION_GRANTED) -> getLastLocation(this::setupViewData)


                else -> {
                    showSnackbar(
                        R.string.permission_denied_explanation, R.string.settings
                    ) {
                        // Construye el intent que muestra la ventana de configuración del app.
                        val intent = Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                    }
                }
            }
        }
    }

    /**
     * Muestra el [Snackbar].
     *
     * @param snackStrId El id del recurso para el el texto en el Snackbar.
     * @param actionStrId El texto para el elemento de acción.
     * @param listener El listener asociado con la acción del Snackbar.
     */
    private fun showSnackbar(
        snackStrId: Int,
        actionStrId: Int = 0,
        listener: View.OnClickListener? = null
    ) {
        val snackbar = Snackbar.make(
            requireActivity().findViewById(android.R.id.content), getString(snackStrId),
            BaseTransientBottomBar.LENGTH_INDEFINITE
        )
        if (actionStrId != 0 && listener != null) {
            snackbar.setAction(getString(actionStrId), listener)
        }
        snackbar.show()
    }
}