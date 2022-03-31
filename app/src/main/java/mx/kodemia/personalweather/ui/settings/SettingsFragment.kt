package mx.kodemia.personalweather.ui.settings

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceFragmentCompat
import mx.kodemia.personalweather.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        val view: View = super.onCreateView(inflater, container, savedInstanceState)
        view.setBackgroundColor(requireContext().getColor(R.color.main_bg))
        return view
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.setGroupVisible(0,false)
    }
}