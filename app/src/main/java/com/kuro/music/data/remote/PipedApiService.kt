package com.kuro.music.data.remote

import com.kuro.music.data.remote.dto.PipedSearchResponse
import com.kuro.music.data.remote.dto.PipedStreamResponse
import com.kuro.music.data.remote.dto.PipedTrendingItem
import com.kuro.music.data.remote.dto.PipedChannelResponse
import com.kuro.music.data.remote.dto.PipedPlaylistResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PipedApiService {

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("filter") filter: String = "music_songs"
    ): PipedSearchResponse

    @GET("suggestions")
    suspend fun getSuggestions(
        @Query("query") query: String
    ): List<String>

    @GET("streams/{videoId}")
    suspend fun getStreams(
        @Path("videoId") videoId: String
    ): PipedStreamResponse

    @GET("trending")
    suspend fun getTrending(
        @Query("region") region: String = "US"
    ): List<PipedTrendingItem>

    @GET("channel/{channelId}")
    suspend fun getChannel(
        @Path("channelId") channelId: String
    ): PipedChannelResponse

    @GET("playlists/{playlistId}")
    suspend fun getPlaylist(
        @Path("playlistId") playlistId: String
    ): PipedPlaylistResponse
}
