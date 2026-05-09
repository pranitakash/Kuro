package com.kuro.music.data.repository

import com.kuro.music.data.local.dao.HistoryDao
import com.kuro.music.data.local.dao.LikedSongDao
import com.kuro.music.data.local.dao.PlaylistDao
import com.kuro.music.data.local.dao.SongDao
import com.kuro.music.data.local.entity.HistoryEntity
import com.kuro.music.data.local.entity.LikedSongEntity
import com.kuro.music.data.local.entity.PlaylistEntity
import com.kuro.music.data.local.entity.PlaylistSongCrossRef
import com.kuro.music.data.local.entity.SongEntity
import com.kuro.music.data.mapper.toEntity
import com.kuro.music.data.mapper.toSong
import com.kuro.music.domain.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryRepository @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val likedSongDao: LikedSongDao,
    private val historyDao: HistoryDao,
    private val songDao: SongDao
) {
    // ─── Liked Songs ───
    fun getLikedSongs(): Flow<List<Song>> =
        likedSongDao.getLikedSongs().map { entities -> entities.map { it.toSong() } }

    fun getLikedSongCount(): Flow<Int> = likedSongDao.getLikedSongCount()

    fun isSongLiked(songId: String): Flow<Boolean> = likedSongDao.isSongLiked(songId)

    suspend fun toggleLike(song: Song) {
        songDao.insertSong(song.toEntity())
        val isCurrentlyLiked = try {
            likedSongDao.isSongLikedSync(song.id)
        } catch (_: Exception) { false }

        if (isCurrentlyLiked) {
            likedSongDao.unlikeSong(song.id)
        } else {
            likedSongDao.likeSong(LikedSongEntity(songId = song.id))
        }
    }

    // ─── Playlists ───
    fun getAllPlaylists(): Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()

    suspend fun createPlaylist(name: String): String {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        playlistDao.insertPlaylist(
            PlaylistEntity(
                id = id,
                name = name,
                createdAt = now,
                updatedAt = now
            )
        )
        return id
    }

    suspend fun renamePlaylist(id: String, name: String) {
        playlistDao.renamePlaylist(id, name)
    }

    suspend fun deletePlaylist(id: String) {
        playlistDao.clearPlaylist(id)
        playlistDao.deletePlaylist(id)
    }

    fun getPlaylistSongs(playlistId: String): Flow<List<Song>> =
        playlistDao.getPlaylistSongs(playlistId).map { entities -> entities.map { it.toSong() } }

    suspend fun addSongToPlaylist(playlistId: String, song: Song) {
        songDao.insertSong(song.toEntity())
        val count = playlistDao.getPlaylistSongCount(playlistId)
        playlistDao.insertPlaylistSong(
            PlaylistSongCrossRef(
                playlistId = playlistId,
                songId = song.id,
                position = count
            )
        )
        playlistDao.renamePlaylist(playlistId, playlistDao.getPlaylistById(playlistId)?.name ?: "")
    }

    suspend fun removeSongFromPlaylist(playlistId: String, songId: String) {
        playlistDao.removeFromPlaylist(playlistId, songId)
    }

    suspend fun getPlaylistById(id: String): PlaylistEntity? = playlistDao.getPlaylistById(id)

    // ─── History ───
    fun getHistory(): Flow<List<HistoryEntity>> = historyDao.getHistory(200)

    fun getRecentlyPlayed(): Flow<List<Song>> =
        historyDao.getRecentlyPlayed(50).map { entities -> entities.map { it.toSong() } }

    suspend fun addToHistory(song: Song) {
        songDao.insertSong(song.toEntity())
        historyDao.insertHistory(HistoryEntity(songId = song.id))
    }

    suspend fun deleteHistoryEntry(id: Long) {
        historyDao.deleteHistoryEntry(id)
    }

    suspend fun clearHistory() {
        historyDao.clearHistory()
    }

    // ─── Downloads ───
    fun getDownloadedSongs(): Flow<List<Song>> =
        songDao.getDownloadedSongs().map { entities -> entities.map { it.toSong() } }
}
