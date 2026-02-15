package com.jorge.mysound.data.repository

import com.jorge.mysound.data.remote.MusicApiService
import com.jorge.mysound.data.remote.SongResponse

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
}