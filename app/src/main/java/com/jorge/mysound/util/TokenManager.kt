package com.jorge.mysound.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {

    // 1. Variable en RAM: Se borra sola al cerrar la app (Muerte súbita)
    private var memoryToken: String? = null

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secret_shared_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // 2. Modificamos saveToken para recibir la decisión del usuario
    fun saveToken(token: String, rememberMe: Boolean = false) { // Default true por si acaso
        // SIEMPRE guardamos en RAM para que la sesión actual funcione
        memoryToken = token

        val editor = sharedPreferences.edit()

        if (rememberMe) {
            // Si quiere ser recordado -> A LA CAJA FUERTE
            editor.putString("jwt_token", token)
        } else {
            // Si NO quiere -> SOLO RAM (y limpiamos disco por si había algo viejo)
            editor.remove("jwt_token")
        }
        editor.apply()
    }

    // 3. Al pedir el token, miramos primero en el bolsillo (RAM), luego en la caja fuerte (Disco)
    fun getToken(): String? {
        return memoryToken ?: sharedPreferences.getString("jwt_token", null)
    }

    fun clearToken() {
        memoryToken = null
        sharedPreferences.edit().remove("jwt_token").apply()
    }
}