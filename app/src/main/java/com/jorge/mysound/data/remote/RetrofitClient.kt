package com.jorge.mysound.data.remote

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.jorge.mysound.util.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * RetrofitClient: Singleton encargado de la configuración y gestión del cliente HTTP.
 * Implementa interceptores para la inyección automática de seguridad (JWT) y
 * logs de red para auditoría en tiempo de desarrollo.
 */
object RetrofitClient {
    // Endpoint principal del servidor backend
    const val BASE_URL = "http://98.85.49.80:8080/"

    @Volatile
    private var apiInstance: MusicApiService? = null

    /**
     * Proporciona una instancia única y thread-safe de MusicApiService.
     * Utiliza el patrón Double-Checked Locking para optimizar el rendimiento.
     */
    fun getInstance(context: Context): MusicApiService {
        return apiInstance ?: synchronized(this) {
            val instance = buildRetrofit(context)
            apiInstance = instance
            instance
        }
    }

    private fun buildRetrofit(context: Context): MusicApiService {
        val tokenManager = TokenManager(context)

        // Configuración de GSON para el manejo estandarizado de fechas ISO 8601
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create()

        /**
         * AuthInterceptor: Intercepta cada petición saliente para adjuntar el
         * token de autorización Bearer recuperado desde el almacenamiento seguro.
         */
        val authInterceptor = Interceptor { chain ->
            val token = tokenManager.getToken()
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()

            if (!token.isNullOrEmpty()) {
                // Inyectamos el header de seguridad de forma centralizada
                requestBuilder.header("Authorization", "Bearer $token")
            } else {
                Log.w("Network_Auth", "Solicitud enviada sin token de acceso (Sesión no iniciada)")
            }

            chain.proceed(requestBuilder.build())
        }

        /**
         * LoggingInterceptor: Proporciona visibilidad completa de las tramas HTTP
         * (Headers, Body, Status Codes) en la consola de depuración.
         */
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Configuración del cliente OkHttp con políticas de reintentos y timeouts
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(MusicApiService::class.java)
    }
}