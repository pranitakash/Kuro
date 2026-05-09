package com.kuro.music.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kuro.music.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: String): SongEntity?

    @Query("SELECT * FROM songs WHERE is_downloaded = 1")
    fun getDownloadedSongs(): Flow<List<SongEntity>>

    @Update
    suspend fun updateSong(song: SongEntity)

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSong(id: String)

    @Query("UPDATE songs SET cached_stream_url = :url, cache_expiry = :expiry WHERE id = :id")
    suspend fun updateStreamCache(id: String, url: String, expiry: Long)

    @Query("SELECT cached_stream_url FROM songs WHERE id = :id AND cache_expiry > :now")
    suspend fun getCachedStreamUrl(id: String, now: Long = System.currentTimeMillis()): String?
}
