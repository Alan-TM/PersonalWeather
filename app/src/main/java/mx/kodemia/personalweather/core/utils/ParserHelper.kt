package mx.kodemia.personalweather.core.utils

import java.text.SimpleDateFormat
import java.util.*

fun hourParser(time: Long): String = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(time * 1000))

fun capitalizeText(text: String): String {
    return if (text.isNotEmpty())
        text.uppercase()
    else ""
}

fun dayParser(day: Long): String = SimpleDateFormat("EEEE", Locale.ENGLISH).format(Date(day * 1000))