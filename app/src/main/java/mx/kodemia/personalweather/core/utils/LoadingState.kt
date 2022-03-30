package mx.kodemia.personalweather.core.utils

sealed class LoadingState{
    data class Loading(val loading: Boolean = true)
    data class Error(val error: String)
}
