package com.kuro.music.di

import android.content.Context
import androidx.room.Room
import com.kuro.music.data.local.KuroDatabase
import com.kuro.music.data.local.dao.HistoryDao
import com.kuro.music.data.local.dao.LikedSongDao
import com.kuro.music.data.local.dao.PlaylistDao
import com.kuro.music.data.local.dao.SongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KuroDatabase {
        val db = Room.databaseBuilder(
            context,
            KuroDatabase::class.java,
            "kuro_database"
        ).build()
        // Register with singleton so DownloadWorker (non-Hilt) reuses the same DB
        KuroDatabase.setInstance(db)
        return db
    }

    @Provides
    fun provideSongDao(db: KuroDatabase): SongDao = db.songDao()

    @Provides
    fun providePlaylistDao(db: KuroDatabase): PlaylistDao = db.playlistDao()

    @Provides
    fun provideHistoryDao(db: KuroDatabase): HistoryDao = db.historyDao()

    @Provides
    fun provideLikedSongDao(db: KuroDatabase): LikedSongDao = db.likedSongDao()

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): com.kuro.music.data.local.UserPreferences {
        return com.kuro.music.data.local.UserPreferences(context)
    }
}
