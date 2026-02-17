package com.jorge.mysound.util

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.json.JSONObject

/**
 * TokenManager: Clase encargada de la gestión segura de tokens JWT.
 * Implementa almacenamiento cifrado para persistencia y almacenamiento en memoria
 * para sesiones temporales.
 */
class TokenManager(context: Context) {

    // Almacenamiento en memoria volátil (se pierde al cerrar el proceso de la app)
    private var memoryToken: String? = null

    // Configuración de la llave maestra para el cifrado de las SharedPreferences
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // Inicialización de SharedPreferences con cifrado de nivel bancario (AES256)
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_user_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Guarda el token de acceso.
     * Si [rememberMe] es verdadero, el token se persiste en el almacenamiento cifrado.
     * En cualquier caso, se mantiene en memoria para el uso de la sesión actual.
     */
    fun saveToken(token: String, rememberMe: Boolean = false) {
        memoryToken = token

        val editor = sharedPreferences.edit()
        if (rememberMe) {
            editor.putString("jwt_token", token)
        } else {
            // Si el usuario no desea ser recordado, eliminamos cualquier token persistido previo
            editor.remove("jwt_token")
        }
        editor.apply()
    }

    /**
     * Recupera el token de acceso activo.
     * Prioriza el token en memoria por rendimiento; si no existe, busca en el almacenamiento persistente.
     */
    fun getToken(): String? {
        return memoryToken ?: sharedPreferences.getString("jwt_token", null)
    }

    /**
     * Elimina el rastro del token tanto en memoria como en almacenamiento persistente.
     * Utilizado durante el proceso de cierre de sesión (Logout).
     */
    fun clearToken() {
        memoryToken = null
        sharedPreferences.edit().remove("jwt_token").apply()
    }

    /**
     * Extrae el identificador de usuario (userId) del Payload del JWT.
     * El proceso decodifica la sección Base64 del token sin necesidad de validación de firma
     * en el lado del cliente, asumiendo que el token es estructuralmente válido.
     */
    fun getUserIdFromToken(): Long {
        val token = getToken() ?: return -1L

        return try {
            // El formato estándar de un JWT es Header.Payload.Signature
            val parts = token.split(".")
            if (parts.size < 2) return -1L

            // Decodificación de la sección de reclamos (Payload)
            val payloadBase64 = parts[1]
            val decodedBytes = Base64.decode(payloadBase64, Base64.URL_SAFE)
            val payloadString = String(decodedBytes, Charsets.UTF_8)

            val jsonObject = JSONObject(payloadString)

            // Verificación de los claims estándar para obtener el ID de usuario
            when {
                jsonObject.has("userId") -> jsonObject.getLong("userId")
                jsonObject.has("sub") -> {
                    // Intento de conversión del subject a Long si el backend lo define como tal
                    jsonObject.getString("sub").toLongOrNull() ?: -1L
                }
                else -> -1L
            }
        } catch (e: Exception) {
            Log.e("Security_TokenManager", "Error al procesar el payload del JWT: ${e.message}")
            -1L
        }
    }
}