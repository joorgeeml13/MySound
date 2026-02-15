package com.jorge.mysound.data.remote

import android.content.Context
import com.jorge.mysound.util.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.google.gson.GsonBuilder

object RetrofitClient {
    const val BASE_URL = "http://98.85.49.80:8080/" // Asegúrate de que termine en /

    @Volatile
    private var apiInstance: MusicApiService? = null

    fun getInstance(context: Context): MusicApiService {
        return apiInstance ?: synchronized(this) {
            val instance = buildRetrofit(context)
            apiInstance = instance
            instance
        }
    }

    private fun buildRetrofit(context: Context): MusicApiService {
        val tokenManager = TokenManager(context)

        val gson = GsonBuilder()
            .setDateFormat("dd/MM/yyyy") // <--- AQUÍ LE DECIMOS EL FORMATO QUE USAS
            .create()

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val authInterceptor = AuthInterceptor(tokenManager)

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MusicApiService::class.java)
    }
}