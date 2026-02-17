package com.jorge.mysound.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Modelos de datos para la comunicación con la API (DTOs).
 * Estos objetos representan la estructura exacta de las respuestas JSON del servidor.
 */

data class SongResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("album") val album: String,
    @SerializedName("fileName") val fileName: String,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("artists") val artists: List<ArtistDto>,
    @SerializedName("genres") val genres: List<GenreDto>
)

data class ArtistDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("imageUrl") val imageUrl: String?
)

data class GenreDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String
)

data class AuthResponse(
    @SerializedName("token") val token: String,
    @SerializedName("username") val username: String?
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("username") val username: String?,
    @SerializedName("birthDate") val birthDate: String
)

data class Playlist(
    @SerializedName("id") val id: Long?,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("songs") val songs: List<SongResponse> = emptyList(),
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("imageUrl") val imageUrl: String?
)

data class UserProfile(
    @SerializedName("id") val id: Long,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("avatarUrl") val avatarUrl: String?,
    @SerializedName("birthDate") val birthDate: String?,
    @SerializedName("favoriteGenre") val favoriteGenre: String,
    @SerializedName("totalSongsInPlaylists") val totalSongsInPlaylists: Long,
)

data class AvatarResponse(
    @SerializedName("url") val url: String
)

/**
 * HomeResponse: Encapsula la información necesaria para poblar la pantalla de inicio.
 * Incluye un perfil sugerido (Match) basado en afinidad musical.
 */
data class HomeResponse(
    @SerializedName("playlists") val playlists: List<Playlist>,
    @SerializedName("matchedUser") val matchedUser: UserProfile?
)