package com.jorge.mysound.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * MusicApiService: Definición de la API REST del cliente.
 * Mapea los endpoints del servidor Spring Boot a funciones suspendidas de Kotlin.
 *
 * NOTA DE ARQUITECTURA:
 * - Se eliminan las barras iniciales ('/') para evitar conflictos con la BaseUrl de Retrofit.
 * - Algunos endpoints devuelven 'Response<T>' para gestionar códigos HTTP (404, 401) manualmente,
 * mientras que otros devuelven 'T' directamente para que el flujo sea más directo (Fail-Fast).
 */
interface MusicApiService {

    // ============================================================================================
    // 🔐 AUTHENTICATION
    // ============================================================================================

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    // ============================================================================================
    // 🎵 SONGS & DISCOVERY
    // ============================================================================================

    @GET("api/songs")
    suspend fun getAllSongs(): List<SongResponse>

    @GET("api/songs/search")
    suspend fun searchSongs(@Query("query") query: String): List<SongResponse>

    @GET("api/songs/recommendations/{id}")
    suspend fun getRecommendations(@Path("id") songId: Long): Response<List<SongResponse>>

    @GET("api/home")
    suspend fun getHomeData(): Response<HomeResponse>

    // ============================================================================================
    // 📜 PLAYLISTS
    // ============================================================================================

    @GET("api/playlists")
    suspend fun getPlaylists(): List<Playlist> // Lista global o del usuario según backend

    @POST("api/playlists")
    suspend fun createPlaylist(@Body playlist: Playlist): Playlist

    @GET("api/playlists/{id}")
    suspend fun getPlaylistById(@Path("id") id: Long): Playlist

    @POST("api/playlists/{playlistId}/songs/{songId}")
    suspend fun addSongToPlaylist(
        @Path("playlistId") playlistId: Long,
        @Path("songId") songId: Long
    ): Response<Unit>

    @DELETE("api/playlists/{playlistId}/songs/{songId}")
    suspend fun removeSongFromPlaylist(
        @Path("playlistId") playlistId: Long,
        @Path("songId") songId: Long
    ): Response<Void>

    @Multipart
    @POST("api/playlists/{id}/image")
    suspend fun uploadPlaylistImage(
        @Path("id") id: Long,
        @Part image: MultipartBody.Part
    ) : Playlist

    // ⚠️ OJO: Verifica si esta ruta es correcta en tu Backend.
    // Lo estándar sería: "api/users/{userId}/playlists"
    @GET("api/playlists/users/{userId}/playlists")
    suspend fun getUserPlaylists(
        @Path("userId") userId: Long
    ): List<Playlist>

    // ============================================================================================
    // 👤 USER PROFILE & SOCIAL
    // ============================================================================================

    @GET("api/users/{id}/profile")
    suspend fun getUserProfile(
        @Path("id") id: Long
    ): Response<UserProfile>

    @Multipart
    @PATCH("api/users/{id}/avatar")
    suspend fun updateAvatar(
        @Path("id") id: Long,
        @Part file: MultipartBody.Part // Coincide con @RequestParam("file") en Spring
    ): Response<AvatarResponse>

    @GET("api/users/{id}/match")
    suspend fun getSimilarUser(
        @Path("id") id: Long
    ): UserProfile
}