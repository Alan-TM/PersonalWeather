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
import androidx.fragment.app.activityViewModels
import coil.load
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import mx.kodemia.personalweather.BuildConfig.APPLICATION_ID
import mx.kodemia.personalweather.databinding.FragmentHomeBinding
import mx.kodemia.personalweather.model.city.City
import mx.kodemia.personalweather.model.weather.WeatherEntity
import mx.kodemia.personalweather.utils.CustomSnackbar
import mx.kodemia.personalweather.utils.checkForInternet
import mx.kodemia.personalweather.utils.showIconHelper
import mx.kodemia.personalweather.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "MainActivityError"
private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var latitude = ""
    private var longitude = ""

    private var units = false
    private var language = false

    private val viewModel: HomeViewModel by activityViewModels()

    private lateinit var customSnackbar: CustomSnackbar

    /**
     * Punto de entrada para el API Fused Location Provider.
     */
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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
        customSnackbar = CustomSnackbar(requireActivity())

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
        observers()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setupViewData(location: Location) {

        if (checkForInternet(requireContext())) {
            // Se coloca en este punto para permitir su ejecución
            showIndicator(true)
            latitude = location.latitude.toString()
            longitude = location.longitude.toString()
            var unit = "metric"
            var languageCode = "es"

            if (units) {
                unit = "imperial"
            }
            if (language) {
                languageCode = "en"
            }

            viewModel.getCityAndWeather(latitude, longitude, unit, languageCode)
        } else {
            showError(getString(R.string.no_internet_access))
            binding.detailsContainer.isVisible = false
        }
    }

    private fun observers() {
        viewModel.cityResponse.observe(viewLifecycleOwner) { city ->
            formatCityText(city)
        }

        viewModel.weatherResponse.observe(viewLifecycleOwner){weather ->
            formatWeatherResponse(weather)
        }

        viewModel.isLoading.observe(viewLifecycleOwner){
            showIndicator(it)
            if(!it)
                applyAnimations()
        }
    }

    /**
     * Función para mostrar los datos obtenidos de OpenWeather
     */

    private fun formatCityText(city: City){
        binding.addressTextView.text = getString(R.string.city, city.name, city.country)
    }

    private fun formatWeatherResponse(weatherEntity: WeatherEntity) {
        val unitSymbol = if (units) "ºF" else "ºC"

        try {
            val temp = "${weatherEntity.current.temp.toInt()} $unitSymbol"
            val dt = weatherEntity.current.dt
            val updateAt = getString(
                R.string.updatedAt,
                SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(dt * 1000))
            )
            val tempMin = "" //"Mín: ${weatherEntity.main.temp_min.toInt()}º"
            val tempMax = "" //""Max: ${weatherEntity.main.temp_max.toInt()}º"
            // Capitalizar la primera letra de la descripción
            var status = ""
            val weatherDescription = weatherEntity.current.weather[0].description
            if (weatherDescription.isNotEmpty()) {
                status = (weatherDescription[0].uppercaseChar() + weatherDescription.substring(1))
            }
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

            binding.apply {
                iconImageView.load(showIconHelper(icon))
                //recuerda cambiarlo a address cuando cambien al binding de main activity !!!
                dateTextView.text = updateAt
                temperatureTextView.text = temp
                statusTextView.text = status
                tempMinTextView.text = tempMin
                tempMaxTextView.text = tempMax
                sunriseTextView.text = sunriseFormat
                sunsetTextView.text = sunsetFormat
                windTextView.text = wind
                pressureTextView.text = pressure
                humidityTextView.text = humidity
                feelsLikeTextView.text = feelsLike
            }
        } catch (exception: Exception) {
            showError(getString(R.string.error_ocurred))
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun showIndicator(visible: Boolean) {
        binding.progressBarIndicator.isVisible = visible
        binding.detailsContainer.isVisible = !visible
    }

    private fun applyAnimations() {
        val cvAnimation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.headline_card_view_fade_in)
        binding.headlineCardView.animation = cvAnimation

        val detailsSlideRightAnimation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.details_slide_in)
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
                    customSnackbar.showSnackbar(R.string.no_location_detected)
                }
            }
    }

    /**
     * Devuelve el estado de los permisos que se necesitan
     */

    private fun checkPermissions() =
        ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    private fun requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            // Proporciona una explicación adicional al usuario (rationale). Esto ocurre si el usuario
            // niega el permiso previamente pero no marca la casilla "No volver a preguntar".
            Log.i(
                TAG,
                "Muestra explicación rationale para proveer una contexto adicional de porque se requiere el permiso"
            )
            customSnackbar.showSnackbar(R.string.permission_rationale, android.R.string.ok) {
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
                    customSnackbar.showSnackbar(
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


}