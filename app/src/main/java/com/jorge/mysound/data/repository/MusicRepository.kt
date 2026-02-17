package com.jorge.mysound.data.repository

import android.util.Log
import com.jorge.mysound.data.remote.HomeResponse
import com.jorge.mysound.data.remote.MusicApiService
import com.jorge.mysound.data.remote.Playlist
import com.jorge.mysound.data.remote.SongResponse
import com.jorge.mysound.data.remote.UserProfile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * MusicRepository: Abstracción de la fuente de datos de la aplicación.
 * Centraliza las llamadas a la API y gestiona la lógica de red, permitiendo
 * que los ViewModels consuman datos procesados y seguros.
 */
class MusicRepository(private val apiService: MusicApiService) {

    /**
     * Realiza una búsqueda de canciones basada en una cadena de texto.
     */
    suspend fun searchSongs(query: String): Result<List<SongResponse>> {
        return try {
            val response = apiService.searchSongs(query)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Construye la URL de streaming para una canción específica.
     * @param songId Identificador de la pista.
     * @return Dirección URL para el flujo de audio.
     */
    fun getStreamUrl(songId: Long): String {
        // Se utiliza la IP configurada para el acceso al recurso de audio
        return "http://98.85.49.80:8080/api/stream/$songId"
    }

    /**
     * Obtiene recomendaciones musicales basadas en la canción actual.
     */
    suspend fun getRecommendations(currentSongId: Long): List<SongResponse> {
        return try {
            val response = apiService.getRecommendations(currentSongId)
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Recupera el listado global de listas de reproducción.
     */
    suspend fun getPlaylists(): List<Playlist> {
        return try {
            apiService.getPlaylists()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Crea una nueva lista de reproducción en el servidor.
     */
    suspend fun createPlaylist(name: String, description: String): Playlist? {
        val newPlaylist = Playlist(
            id = null,
            name = name,
            description = description,
            imageUrl = null,
            createdAt = null,
            songs = emptyList()
        )
        return try {
            apiService.createPlaylist(newPlaylist)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Vincula una canción a una lista de reproducción específica.
     */
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long): Result<Unit> {
        return try {
            val response = apiService.addSongToPlaylist(playlistId, songId)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error code: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene el detalle completo de una lista de reproducción por su ID.
     */
    suspend fun getPlaylistById(playlistId: Long): Playlist {
        return apiService.getPlaylistById(playlistId)
    }

    /**
     * Sube una imagen de portada para una lista de reproducción.
     */
    suspend fun uploadPlaylistImage(playlistId: Long, file: File): Result<Unit> {
        return try {
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            apiService.uploadPlaylistImage(playlistId, body)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Recupera la información del perfil de un usuario.
     */
    suspend fun getUserProfile(userId: Long): Result<UserProfile> {
        return try {
            val response = apiService.getUserProfile(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error perfil: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza el avatar del usuario mediante una petición multipart.
     */
    suspend fun updateAvatar(userId: Long, file: File): Result<String> {
        return try {
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val response = apiService.updateAvatar(userId, body)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.url)
            } else {
                Result.failure(Exception("Server error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene los datos consolidados para la pantalla de inicio (Match + Playlists).
     */
    suspend fun getHomeData(): HomeResponse? {
        return try {
            val response = apiService.getHomeData()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Log.e("MusicRepository", "Error en getHomeData: ${e.message}")
            null
        }
    }

    /**
     * Obtiene el listado de listas de reproducción creadas por un usuario específico.
     */
    suspend fun getUserPlaylists(userId: Long): List<Playlist> {
        return try {
            apiService.getUserPlaylists(userId)
        } catch (e: Exception) {
            emptyList()
        }
    }
}