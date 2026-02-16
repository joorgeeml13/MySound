package com.jorge.mysound.data.repository

import com.jorge.mysound.data.remote.MusicApiService
import com.jorge.mysound.data.remote.Playlist
import com.jorge.mysound.data.remote.SongResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

// MusicRepository.kt
class MusicRepository(private val apiService: MusicApiService) {

    suspend fun searchSongs(query: String): Result<List<SongResponse>> {
        return try {
            val response = apiService.searchSongs(query)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getStreamUrl(songId: Long): String {
        // Solo devolvemos el String, no hacemos una petición suspendida
        return "http://98.85.49.80:8080/api/stream/$songId"
    }

    suspend fun getRecommendations(currentSongId: Long): List<SongResponse> {
        return try {
            val response = apiService.getRecommendations(currentSongId)
            if (response.isSuccessful && response.body() != null) {
                response.body()!! // Devolvemos la lista de canciones
            } else {
                emptyList() // Si falla, devolvemos lista vacía y no pasa nada
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // Si no hay internet, lista vacía
        }
    }

    // En tu MusicRepository
    suspend fun getPlaylists(): List<Playlist> {
        return apiService.getPlaylists() // Asumiendo que usas Retrofit
    }

    suspend fun createPlaylist(name: String, description: String): Playlist {
        // 🔥 FIX: Usamos "Named Arguments" para ser explícitos y rellenamos los huecos con null
        val newPlaylist = Playlist(
            id = null, // El ID lo genera el backend
            name = name,
            description = description,
            imageUrl = null, // 👈 AQUÍ ESTABA EL ERROR (No hay foto al crearla)
            createdAt = null,
            songs = emptyList()
        )

        return apiService.createPlaylist(newPlaylist)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        apiService.addSongToPlaylist(playlistId, songId)
    }

    suspend fun getPlaylistById(playlistId: Long): Playlist {
        // Directo al grano, si falla Retrofit lanzará la excepción que captura el VM
        return apiService.getPlaylistById(playlistId)
    }

    suspend fun uploadImage(playlistId: Long, file: File) {
        // 1. Convertimos el archivo en un RequestBody
        // "image/*" le dice al servidor que es una imagen, da igual si jpg o png
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())

        // 2. Creamos la parte Multipart
        // "file" <--- ESTE NOMBRE TIENE QUE COINCIDIR CON EL BACKEND EXCATAMENTE
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        // 3. ¡Fuego!
        apiService.uploadPlaylistImage(playlistId, body)
    }
}