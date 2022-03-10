package mx.kodemia.personalweather.utils

import android.app.Activity
import android.view.View
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

class CustomSnackbar(private val activity: Activity) {

    fun showSnackbar(
        snackStrId: Int,
        actionStrId: Int = 0,
        listener: View.OnClickListener? = null
    ) {
        val snackbar = Snackbar.make(
            activity.findViewById(android.R.id.content), activity.getString(snackStrId),
            BaseTransientBottomBar.LENGTH_INDEFINITE
        )
        if (actionStrId != 0 && listener != null) {
            snackbar.setAction(activity.getString(actionStrId), listener)
        }
        snackbar.show()
    }
}