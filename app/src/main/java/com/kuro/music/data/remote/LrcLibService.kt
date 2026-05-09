package com.kuro.music.data.remote

import com.kuro.music.data.remote.dto.LrcLibResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface LrcLibService {

    @GET("api/get")
    suspend fun getLyrics(
        @Query("artist_name") artistName: String,
        @Query("track_name") trackName: String,
        @Query("album_name") albumName: String = "",
        @Query("duration") duration: Int? = null
    ): LrcLibResponse

    @GET("api/search")
    suspend fun searchLyrics(
        @Query("q") query: String
    ): List<LrcLibResponse>
}
