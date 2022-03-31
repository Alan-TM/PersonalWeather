package mx.kodemia.personalweather.core.utils

import android.app.Activity
import android.view.View
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

class CustomSnack(private val activity: Activity) {

    fun showSnack(
        snackStrId: Int,
        actionStrId: Int = 0,
        listener: View.OnClickListener? = null
    ) {
        val snack = Snackbar.make(
            activity.findViewById(android.R.id.content), activity.getString(snackStrId),
            BaseTransientBottomBar.LENGTH_INDEFINITE
        )
        if (actionStrId != 0 && listener != null) {
            snack.setAction(activity.getString(actionStrId), listener)
        }
        snack.show()
    }
}