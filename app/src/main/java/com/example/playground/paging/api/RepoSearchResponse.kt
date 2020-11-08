package com.example.playground.paging.api

import com.example.playground.paging.RepoModel
import com.google.gson.annotations.SerializedName

/**
 * Data class to hold repo responses from the searchRepo API calls
 */
data class RepoSearchResponse(
    @SerializedName("total_count") val total: Int = 0,
    @SerializedName("items") val items: List<RepoModel> = emptyList(),
    val nextPage: Int? = null
)