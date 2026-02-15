package com.jorge.mysound.data.remote

import java.time.LocalDate

// SongResponse.kt
data class SongResponse(
    val id: Long,
    val title: String,
    val album: String,
    val fileName: String,
    val imageUrl: String?, // La URL de la carátula que extrajimos
    val artists: List<ArtistDto>,
    val genres: List<GenreDto>
)

data class ArtistDto(val id: Long, val name: String, val imageUrl: String?)
data class GenreDto(val id: Long, val name: String)

data class AuthResponse(val token: String, val username: String?)
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val email: String, val password: String, val username: String?, val birthDate: String)

