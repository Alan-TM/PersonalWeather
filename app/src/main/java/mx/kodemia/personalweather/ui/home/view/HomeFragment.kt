@file:Suppress("DEPRECATION")

package mx.kodemia.personalweather.ui.home.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import mx.kodemia.personalweather.BuildConfig.APPLICATION_ID
import mx.kodemia.personalweather.R
import mx.kodemia.personalweather.adapters.WeatherDailyAdapter
import mx.kodemia.personalweather.core.Constants.ERROR_IO
import mx.kodemia.personalweather.core.Constants.ERROR_NOT_FOUND
import mx.kodemia.personalweather.core.Constants.ERROR_NO_INTERNET
import mx.kodemia.personalweather.core.Constants.ERROR_UNAUTHORIZED
import mx.kodemia.personalweather.core.Constants.REQUEST_PERMISSIONS_REQUEST_CODE
import mx.kodemia.personalweather.core.utils.CustomSnack
import mx.kodemia.personalweather.core.utils.checkForInternet
import mx.kodemia.personalweather.core.utils.showIconHelper
import mx.kodemia.personalweather.databinding.FragmentHomeBinding
import mx.kodemia.personalweather.ui.home.viewmodel.HomeViewModel

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var sharedPrefUnits = false
    private var sharedPrefLanguage = false

    private val viewModel: HomeViewModel by activityViewModels()

    private lateinit var customSnack: CustomSnack

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setHideToolbar(false)
        customSnack = CustomSnack(requireActivity())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        permissionsSetup()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        sharedPrefUnits = sharedPreferences.getBoolean("units", false)
        sharedPrefLanguage = sharedPreferences.getBoolean("language", false)

        onRefreshAPICall()
        apiResponseObservers()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun permissionsSetup() {
        if (checkForInternet(requireContext())) {
            if (!checkPermissions()) {
                requestPermissions()
            } else {
                getLastLocation { location ->
                    setupViewData(location)
                }
            }
        } else {
            viewModel.setErrorCode(ERROR_NO_INTERNET)
        }
    }

    private fun onRefreshAPICall() {
        with(binding.swipeRefreshRootLayout) {
            setOnRefreshListener {
                permissionsSetup()
                this.isRefreshing = false
            }
        }
    }

    private fun setupViewData(location: Location) {
        viewModel.setDataForAPICall(
            location.latitude.toString(),
            location.longitude.toString(),
            sharedPrefUnits,
            sharedPrefLanguage
        )
        viewModel.getCityAndWeather()
    }

    private fun apiResponseObservers() {
        with(viewModel) {
            cityResponse.observe(viewLifecycleOwner) { city ->
                binding.addressTextView.text = getString(R.string.city, city.name, city.country)
            }

            weatherDataForView.observe(viewLifecycleOwner, ::setWeatherInfo)

            weatherDaily.observe(viewLifecycleOwner, ::setupRecycler)

            isLoading.observe(viewLifecycleOwner) {
                showLoadingIndicator(it)
                if (!it)
                    applyAnimations()
            }

            errorCode.observe(viewLifecycleOwner){ code ->
                when(code){
                    ERROR_NO_INTERNET -> findNavController().navigate(R.id.action_homeFragment_to_noInternetFragment)
                    ERROR_IO -> showMessage(getString(R.string.error_ocurred))
                    ERROR_UNAUTHORIZED -> customSnack.showSnack(R.string.unauthorized)
                    ERROR_NOT_FOUND -> customSnack.showSnack(R.string.not_found)
                }
            }
        }
    }

    private fun setupRecycler(daily: ArrayList<HashMap<String, String>>) {
        binding.recyclerDailyWeather.apply {
            adapter = WeatherDailyAdapter(daily)
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setWeatherInfo(data: HashMap<String, String>) {
        with(binding) {
            iconImageView.load(showIconHelper(data["icon"]!!))
            dateTextView.text = data["updatedAt"]
            temperatureTextView.text = data["temperature"]
            statusTextView.text = data["status"]
            sunriseTextView.text = data["sunrise"]
            sunsetTextView.text = data["sunset"]
            windTextView.text = getString(R.string.wind_value, data["wind"])
            pressureTextView.text = getString(R.string.pressure_value, data["pressure"])
            humidityTextView.text = data["humidity"]
            feelsLikeTextView.text = getString(R.string.sensation, data["feelsLike"])
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun showLoadingIndicator(visible: Boolean) {
        with(binding) {
            progressBarIndicator.isVisible = visible
            headlineCardView.isVisible = !visible
            detailsContainer.isVisible = !visible
            addressTextView.isVisible = !visible
            recyclerDailyWeather.isVisible = !visible
        }
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
        fusedLocationClient.lastLocation
            .addOnCompleteListener { taskLocation ->
                if (taskLocation.isSuccessful && taskLocation.result != null) {
                    onLocation(taskLocation.result)
                } else {
                    customSnack.showSnack(R.string.no_location_detected)
                }
            }
    }

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
            customSnack.showSnack(R.string.permission_rationale, android.R.string.ok) {
                startLocationPermissionRequest()
            }
        } else {
            startLocationPermissionRequest()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> showMessage(getString(R.string.canceled_action))
                (grantResults[0] == PackageManager.PERMISSION_GRANTED) -> getLastLocation(this::setupViewData)
                else -> {
                    customSnack.showSnack(
                        R.string.permission_denied_explanation, R.string.settings
                    ) {
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