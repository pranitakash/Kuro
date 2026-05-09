package com.kuro.music.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.kuro.music.data.local.entity.HistoryEntity
import com.kuro.music.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert
    suspend fun insertHistory(history: HistoryEntity)

    @Transaction
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN (
            SELECT song_id, MAX(played_at) AS latest_played
            FROM history
            GROUP BY song_id
        ) h ON s.id = h.song_id
        ORDER BY h.latest_played DESC
        LIMIT :limit
    """)
    fun getRecentlyPlayed(limit: Int = 50): Flow<List<SongEntity>>

    @Query("SELECT * FROM history ORDER BY played_at DESC LIMIT :limit")
    fun getHistory(limit: Int = 100): Flow<List<HistoryEntity>>

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteHistoryEntry(id: Long)

    @Query("DELETE FROM history")
    suspend fun clearHistory()
}
