package mx.kodemia.personalweather.utils

import java.text.SimpleDateFormat
import java.util.*

fun hourParser(time: Long): String = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(time * 1000))

fun capitalizeFirstLetterText(text: String): String {
    return if (text.isNotEmpty())
        (text.uppercase() + text.substring(1))
    else ""
}