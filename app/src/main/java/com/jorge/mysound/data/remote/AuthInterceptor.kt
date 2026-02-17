package com.jorge.mysound.data.remote

import com.jorge.mysound.util.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * AuthInterceptor: Interceptor de red encargado de la gestión centralizada de la
 * seguridad en las peticiones HTTP.
 * * Su función principal es inyectar el token de portador (Bearer Token) en la cabecera
 * de autorización de forma automática para todas las peticiones que lo requieran.
 */
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenManager.getToken()

        /**
         * Si el token es inexistente (ej. usuario no autenticado o pantallas de acceso),
         * procedemos con la petición original sin modificar las cabeceras.
         */
        if (token == null) {
            return chain.proceed(originalRequest)
        }

        /**
         * Si disponemos de un token activo, reconstruimos la petición inyectando
         * la cabecera 'Authorization'. Se utiliza el método .header() para garantizar
         * la unicidad de la cabecera y evitar duplicados en reintentos de red.
         */
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(newRequest)
    }
}