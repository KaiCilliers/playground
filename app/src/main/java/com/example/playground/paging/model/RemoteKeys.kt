package com.example.playground.paging.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * When we get the last item loaded from the PagingState,
 * there's no way to know the index of the page it belonged
 * to. To solve this problem, we add another table that
 * stores the next and previous page key for each RepoModel
 */
@Entity(tableName = "remote_keys")
class RemoteKeys (
    @PrimaryKey
    val repoId: Long,
    // Both can be null when we can't append or prepend data
    val prevKey: Int?,
    val nextKey: Int?
)