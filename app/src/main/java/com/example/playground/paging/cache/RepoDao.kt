package com.example.playground.paging.cache

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.playground.paging.model.RepoModel

@Dao
interface RepoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(repos: List<RepoModel>)

    @Query("SELECT * FROM repos WHERE name LIKE :queryString OR description LIKE :queryString ORDER BY stars DESC, name ASC")
    fun reposByName(queryString: String): PagingSource<Int, RepoModel>

    @Query("DELETE FROM repos")
    suspend fun clear()
}