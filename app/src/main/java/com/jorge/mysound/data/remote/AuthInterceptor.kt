package com.jorge.mysound.data.remote

import com.jorge.mysound.util.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenManager.getToken()

        // Si no hay token, mandamos la petición tal cual (Login/Register)
        if (token == null) {
            return chain.proceed(originalRequest)
        }

        // Si HAY token, lo pegamos en la frente de la petición
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(newRequest)
    }
}