package mx.kodemia.personalweather.utils

sealed class LoadingState{
    data class Loading(val loading: Boolean = true)
    data class Error(val error: String)
}
