package com.example.playground.paging.data

import androidx.paging.PagingSource
import com.example.playground.paging.RepoModel
import com.example.playground.paging.api.GithubService
import com.example.playground.paging.api.IN_QUALIFIER

// TODO remove constant
private const val  GITHUB_STARTING_PAGE_INDEX = 1

/**
 * We have defined the type of paging key,
 * the type of data loaded, and where the
 * data is retrieved from
 */
class GithubPagingSource (
    private val service: GithubService,
    private val query: String
) : PagingSource<Int, RepoModel>() {
    /**
     * LoadParams object keeps information related
     * to the load operation, like the key of the
     * page to be loaded and the requested number
     * of items to load
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RepoModel> {
        // Paging index (if null assign starting index)
        val position = params.key ?: GITHUB_STARTING_PAGE_INDEX
        // Search string query
        val apiQuery = query + IN_QUALIFIER

        return try {
            // Fetch a batch of data
            val response = service.searchRepos(
                apiQuery, position, params.loadSize
            )
            val repos = response.items
            // return this object containing the data, prev index, and next index
            LoadResult.Page(
                data = repos,
                prevKey = if (position == GITHUB_STARTING_PAGE_INDEX) null else position - 1,
                nextKey = if (repos.isEmpty()) null else position + 1
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }
}