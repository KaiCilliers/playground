package com.example.playground.paging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.example.playground.paging.data.GithubRepository
import com.example.playground.paging.model.RepoModel
import com.example.playground.paging.model.UiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SearchRepoViewModel(private val repository: GithubRepository) : ViewModel() {
    /**
     * These values are used to help ensure whenever a new search
     * query is received that is the same as the current query,
     * we will just return the existing Flow
     */
    private var currentQueryValue: String? = null
    private var currentSearchResult: Flow<PagingData<UiModel>>? = null

    // Extension function to round up the number of stars
    private val UiModel.RepoItem.roundedStarCount: Int
        get() = this.repo.stars / 10_000

    fun searchRepo(queryString: String): Flow<PagingData<UiModel>> {
        val lastResult = currentSearchResult
        if (queryString == currentQueryValue && lastResult != null) {
            return lastResult
        }
        currentQueryValue = queryString
        val newResult = repository.getSearchResultStream(queryString)
            /**
             * We need to transform each RepoModel into a UiModel.RepoItem
             * To do this, we use the Flow.map operator and then map each
             * PagingData to build a new UiModel.RepoItem from the current
             * RepoModel item resulting in a Flow<PagingData
             */
            .map { pagingData -> pagingData.map { UiModel.RepoItem(it) } }
            .map {
                it.insertSeparators<UiModel.RepoItem, UiModel> { before, after ->
                    if (after == null) {
                        // we're at the end of the list
                        return@insertSeparators null
                    }
                    if (before == null) {
                        // we're at the beginning of the list
                        return@insertSeparators UiModel.SeparatorItem("${after.roundedStarCount}0.000+ stars")
                    }
                    // check between 2 items
                    if (before.roundedStarCount > after.roundedStarCount) {
                        if (after.roundedStarCount >= 1) {
                            UiModel.SeparatorItem("${after.roundedStarCount}0.000+ stars")
                        } else {
                            UiModel.SeparatorItem("< 10.000+ stars")
                        }
                    } else {
                        // no seperator
                        null
                    }
                }
            }.cachedIn(viewModelScope)
        currentSearchResult = newResult
        return newResult
    }
}