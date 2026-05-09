package com.kuro.music.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.kuro.music.data.local.entity.LikedSongEntity
import com.kuro.music.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LikedSongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun likeSong(likedSong: LikedSongEntity)

    @Query("DELETE FROM liked_songs WHERE song_id = :songId")
    suspend fun unlikeSong(songId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM liked_songs WHERE song_id = :songId)")
    fun isSongLiked(songId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM liked_songs WHERE song_id = :songId)")
    suspend fun isSongLikedSync(songId: String): Boolean

    @Transaction
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN liked_songs ls ON s.id = ls.song_id
        ORDER BY ls.liked_at DESC
    """)
    fun getLikedSongs(): Flow<List<SongEntity>>

    @Query("SELECT COUNT(*) FROM liked_songs")
    fun getLikedSongCount(): Flow<Int>
}
