package mx.kodemia.personalweather.network.api

import android.icu.util.TimeUnit
import android.util.Log
import mx.kodemia.personalweather.core.Constants
import mx.kodemia.personalweather.core.Constants.BASE_URL
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    fun getInstance(): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .build()
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
