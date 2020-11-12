package com.example.playground.paging.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.playground.paging.model.RepoModel
import com.example.playground.paging.api.GithubService
import com.example.playground.paging.cache.RepoDatabase
import kotlinx.coroutines.flow.Flow

/**
 * Repository class that work with local and remote data sources
 */
//@ExperimentalCoroutinesApi
class GithubRepository(
    private val service: GithubService,
    private val db: RepoDatabase
) {
    // TODO remove constant
    companion object {
        // NOTE - Value should be 50. Six is way too low and
        // just used to test the footer loading icon and
        // retry button. You are making too many network
        // requests by only fetching 6 items per call/page
        private const val NETWORK_PAGE_SIZE = 50
    }
    /**
     * Construct a pager and return it as a Flow object
     *
     * PagingConfig sets options regarding how to load content from a PagingSource.
     * The only mandatory parameter to define is page size. By default, Paging will
     * keep in memory all pages loaded, but you can change that by setting the
     * maxSize parameter in PagingConfig to not waster memory as user scrolls.
     *
     * By default, Paging returns null items as placeholders for content that is not
     * loaded yet if Paging can count the unloaded items and if the enablePlaceholders
     * config flag is true. This allows you to display a placeholder view in your
     * adapter.
     *
     * NOTE - pageSize should be enough for several screens' worth of items
     *
     * NOTE - maxSize is unbound by default, so pages are never dropped. If you
     * want to drop pages, ensure maxSize is high enough to prevent too many
     * network requests as the user keeps changing the scroll direction. The
     * minimum value is pageSize + prefetchDistance * 2
     *
     * NOTE -
     * Prefetch distance which defines how far from the edge of loaded content an access must be to
     * trigger further loading. Typically should be set several times the number of visible items
     * onscreen.
     * E.g., If this value is set to 50, a [PagingData] will attempt to load 50 items in advance of
     * data that's already been accessed.
     * A value of 0 indicates that no list items will be loaded until they are specifically
     * requested. This is generally not recommended, so that users don't observe a
     * placeholder item (with placeholders) or end of list (without) while scrolling.
     *
     */
    fun getSearchResultStream(query: String): Flow<PagingData<RepoModel>> {
        // appending '%' so we can allow other characters to be before and after the query string
        val dbQuery = "%${query.replace(' ','%')}%"
        val pagingSourceFactory = {
            db.repoDao().reposByName(dbQuery)
        }
        return Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = false
            ),
            remoteMediator = GithubRemoteMediator(
                query, service, db
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow
    }
}