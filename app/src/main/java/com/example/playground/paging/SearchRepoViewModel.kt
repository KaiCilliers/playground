package com.example.playground.paging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.playground.paging.data.GithubRepository
import kotlinx.coroutines.flow.Flow

class SearchRepoViewModel(private val repository: GithubRepository) : ViewModel() {
    /**
     * These values are used to help ensure whenever a new search
     * query is received that is the same as the current query,
     * we will just return the existing Flow
     */
    private var currentQueryValue: String? = null
    private var currentSearchResult: Flow<PagingData<RepoModel>>? = null

    fun searchRepo(queryString: String): Flow<PagingData<RepoModel>> {
        val lastResult = currentSearchResult
        if (queryString == currentQueryValue && lastResult != null) {
            return lastResult
        }
        currentQueryValue = queryString
        val newResult = repository.getSearchResultStream(queryString)
            .cachedIn(viewModelScope)
        currentSearchResult = newResult
        return newResult
    }
}