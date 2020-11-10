package com.example.playground.paging.cache

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.playground.paging.model.RemoteKeys
import com.example.playground.paging.model.RepoModel

@Database(
    entities = [RepoModel::class, RemoteKeys::class],
    version = 1,
    exportSchema = false
)
abstract class RepoDatabase : RoomDatabase() {

    abstract fun repoDao(): RepoDao
    abstract fun keysDao(): RemoteKeysDao

    companion object {
        @Volatile
        private var INSTANCE: RepoDatabase? = null
        fun getInstance(context: Context): RepoDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                RepoDatabase::class.java, "Github.db"
            ).build()
    }
}