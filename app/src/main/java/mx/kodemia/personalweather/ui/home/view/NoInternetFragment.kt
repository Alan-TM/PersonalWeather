package mx.kodemia.personalweather.ui.home.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import mx.kodemia.personalweather.R
import mx.kodemia.personalweather.core.utils.checkForInternet
import mx.kodemia.personalweather.databinding.FragmentNoInternetBinding
import mx.kodemia.personalweather.ui.home.viewmodel.HomeViewModel

class NoInternetFragment : Fragment() {

    private var _binding: FragmentNoInternetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        _binding = FragmentNoInternetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setHideToolbar(true)
        refreshForInternet()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.setGroupVisible(0, false)
    }

    private fun refreshForInternet() {
        with(binding.swipeNoInternetContainer) {
            setOnRefreshListener {
                if (checkForInternet(requireContext())) {
                    viewModel.setErrorCode(0)
                    findNavController().navigate(R.id.action_noInternetFragment_to_homeFragment)
                    isRefreshing = false
                } else{
                    binding.swipeNoInternetContainer.isRefreshing = false
                }
            }
        }
    }
}