package mx.kodemia.personalweather

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceFragmentCompat
import mx.kodemia.personalweather.databinding.ActivityMainBinding

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = super.onCreateView(inflater, container, savedInstanceState)
        view.setBackgroundColor(requireContext().getColor(R.color.main_bg))
        binding = ActivityMainBinding.inflate(inflater, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onPrepareOptionsMenu(menu: Menu){
        super.onPrepareOptionsMenu(menu)
        /*val item = menu.findItem(R.id.menu_settings)
        item.isVisible = false*/
        //menu.setGroupVisible(0,false)
        menu.clear()
    }


}