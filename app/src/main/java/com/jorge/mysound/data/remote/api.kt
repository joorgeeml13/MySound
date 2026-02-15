package com.jorge.mysound.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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
}