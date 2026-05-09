package com.kuro.music.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kuro.music.data.local.dao.HistoryDao
import com.kuro.music.data.local.dao.LikedSongDao
import com.kuro.music.data.local.dao.PlaylistDao
import com.kuro.music.data.local.dao.SongDao
import com.kuro.music.data.local.entity.HistoryEntity
import com.kuro.music.data.local.entity.LikedSongEntity
import com.kuro.music.data.local.entity.PlaylistEntity
import com.kuro.music.data.local.entity.PlaylistSongCrossRef
import com.kuro.music.data.local.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        HistoryEntity::class,
        LikedSongEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KuroDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun historyDao(): HistoryDao
    abstract fun likedSongDao(): LikedSongDao

    companion object {
        @Volatile
        private var INSTANCE: KuroDatabase? = null

        fun getInstance(context: Context): KuroDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    KuroDatabase::class.java,
                    "kuro_database"
                )
                    .build()
                    .also { INSTANCE = it }
            }
        }

        /**
         * Called by Hilt's DatabaseModule to register the DI-created instance
         * so that DownloadWorker (which can't use Hilt) uses the same DB.
         */
        fun setInstance(db: KuroDatabase) {
            INSTANCE = db
        }
    }
}
