package com.jorge.mysound.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface MusicApiService {

    @GET("api/songs")
    suspend fun getAllSongs(): List<SongResponse>

    @GET("api/songs/search")
    suspend fun searchSongs(@Query("query") query: String): List<SongResponse>


    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("/api/songs/recommendations/{id}")
    suspend fun getRecommendations(@Path("id") songId: Long): Response<List<SongResponse>>

    @GET("api/playlists")
    suspend fun getPlaylists(): List<Playlist> // Lista todas las playlists del usuario

    @POST("api/playlists")
    suspend fun createPlaylist(@Body playlist: Playlist): Playlist // Crea una nueva

    @GET("api/playlists/{id}")
    suspend fun getPlaylistById(@Path("id") id: Long): Playlist // Trae una con sus canciones

    @POST("api/playlists/{playlistId}/songs/{songId}")
    suspend fun addSongToPlaylist(
        @Path("playlistId") playlistId: Long,
        @Path("songId") songId: Long
    ): Response<Void> // El 200 OK de toda la vida

    @DELETE("api/playlists/{playlistId}/songs/{songId}")
    suspend fun removeSongFromPlaylist(
        @Path("playlistId") playlistId: Long,
        @Path("songId") songId: Long
    ): Response<Void>

    @Multipart
    @POST("api/playlists/{id}/image")
    suspend fun uploadPlaylistImage(
        @Path("id") id: Long,
        @Part image: MultipartBody.Part // 👈 La pieza clave
    ) : Playlist
}