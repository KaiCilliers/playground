package com.example.playground.paging.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.playground.paging.api.GithubService
import com.example.playground.paging.api.IN_QUALIFIER
import com.example.playground.paging.cache.RepoDatabase
import com.example.playground.paging.model.RemoteKeys
import com.example.playground.paging.model.RepoModel
import timber.log.Timber
import java.io.InvalidObjectException
import java.lang.Exception

// TODO remove constant
private const val GITHUB_STARTING_PAGE_INDEX = 1

/**
 * This class will be recreated for every new query,
 * hence the amount of parameters
 *
 * Replaced GithubPagingSource class
 */
@OptIn(ExperimentalPagingApi::class)
class GithubRemoteMediator(
    private val query: String,
    private val service: GithubService,
    private val db: RepoDatabase
) : RemoteMediator<Int, RepoModel>() {
    /**
     * This method will be called whenever we need to load
     * more data from the network.
     *
     * @return MediatorResult object which can either be
     * Error - if an error occurs requesting data from the network
     * Success - successfully got data from the network.
     *
     * @param loadType which tells us whether we need to load data
     * at the end (LoadType.APPEND) or at the beginning of the data
     * (LoadType.PREPEND) that we previously loaded, or if this is
     * the first time we're loading data (LoadType.REFRESH)
     *
     * @param state gives us information about the page that
     * were loaded before, the most recently accessed index in the
     * list, and the PagingConfig we defined when initializing the
     * paging stream
     *
     * TODO bug fix
     * load() is called multiple times to fetch multiple pages of data
     * which causes list to flicker as it updates with each separate
     * network call
     */
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, RepoModel>
    ): MediatorResult {
        Timber.d("mediator load")
        /**
         * Find out what page we need to
         * load from the network, based on
         * the [LoadType]
         */
        val page: Int = when (loadType) {
            // When it's the first time we're loading data, or when PagingDataAdapter.refresh()
            // is called. Point of reference for loading data is state.anchorPosition
            // If this is first load then anchorPosition is null, when refresh() is called
            // the anchorPosition is the first visible position in the displayed list, so
            // we will need to load the page that contains that specific item
            LoadType.REFRESH -> {
                Timber.d("mediator load type refresh")
                // Based on the anchorPosition form state, we can get the closest RepoModel
                // item to that position by calling state.closestItemToPosition()
                // Based on the RepoModel item, we can get the RemoteKeys from the database
                val remoteKeys = remoteKeysClosestToCurrentPosition(state)
                // If the remoteKey is not null we can get the nextKey from it
                // In the Github API the page keys are incremented sequentially,
                // thus to get the page containing the current item, we subtract
                // one from the remoteKeys.nextKey
                // If the remoteKey is null (because anchorPosition was null), then
                // the page we need to load is the initial one: GITHUB_STARTING_PAGE_INDEX
                remoteKeys?.nextKey?.minus(1) ?: GITHUB_STARTING_PAGE_INDEX
            }
            // When we need to load data at the beginning og the currently loaded data set
            LoadType.PREPEND -> {
                Timber.d("mediator loadtype prepend")
                // Based on the first item in the database we need to compute the
                // network page key
                // Get the remote key of the first RepoModel item loaded from the database
                val remoteKeys = remoteKeysForFirstItem(state)
                if(remoteKeys == null) {
                    // The LoadType is PREPEND so some data was loaded before,
                    // so we should have been able to get remote keys
                    // If the remoteKeys are null, then we're an invalid state and we have a bug
                    throw InvalidObjectException("Remote key and the prevKey should not be null")
                }
                // If the previous key is null, then we can't request more data
                val prevKey = remoteKeys.prevKey
                if(prevKey == null) {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                remoteKeys.prevKey
            }
            // When we need to load data at the end of the currently loaded data set
            LoadType.APPEND -> {
                Timber.d("mediator loadtype append")
                // Based on the last item in the database we need to compute the network page key
                // Get the remote key of the last RepoModel item loaded from the database
                val remoteKeys = remoteKeysForLastItem(state)
                if (remoteKeys?.nextKey == null) {
                    throw InvalidObjectException("Remote key should not be null for $loadType")
                }
                remoteKeys.nextKey
            }
        }
        // Complete query
        val apiQuery = query + IN_QUALIFIER
        try {
            /**
             * Trigger the network request
             */
            Timber.d("mediator network call")
            val apiResponse = service.searchRepos(
                apiQuery, page, state.config.pageSize
            )
            Timber.d("Mediator after network call")
            /**
             * Once the network request completes
             *
             * If the received list of repositories is not empty,
             * then compute the RemoteKeys for every RepoModel
             * If this is a new query (REFRESH) then clear
             * the database. Save the RemoteKeys and RepoModels
             * in the database and then:
             * @return MediatorResult.Success(false)
             *
             * If the list of repos was empty then:
             * @return MediatorResult.Success(true)
             *
             * If we get an error requesting data:
             * @return MediatorResult.Error
             */
            val repos = apiResponse.items
            // Query returns empty list if no result is found when requesting a new "page" of RepoModels
            val endOfPaginationReached = repos.isEmpty()
            Timber.d("Repos empty: $endOfPaginationReached")
//            if(!endOfPaginationReached) { // I would add this logic, it makes sense
                // Run like a custom transaction (if single query fails in block then rollback all queries)
                db.withTransaction {
                    Timber.d("inside transaction")
                    // clear all tables in the database if new query
                    if (loadType == LoadType.REFRESH) {
                        db.keysDao().clear()
                        db.repoDao().clear()
                    }
                    // Set previous and next page keys
                    val prevKey = if (page == GITHUB_STARTING_PAGE_INDEX) null else page - 1
                    val nextKey = if (endOfPaginationReached) null else page + 1
                    // Create key objects for each RepoModel
                    // Each RepoModel is going to have the same previous and next page index
                    // which indicates they all belong in the same page of data
                    val keys = repos.map {
                        RemoteKeys(
                            repoId = it.id,
                            prevKey = prevKey,
                            nextKey = nextKey
                        )
                    }
                    // Populate tables
                    db.keysDao().insertAll(keys)
                    db.repoDao().insertAll(repos)
//                }
            }
            Timber.d("before mediator returns: $endOfPaginationReached")
            // @return either true or false
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

    /**
     * LoadType.APPEND
     */
    private suspend fun remoteKeysForLastItem(state: PagingState<Int, RepoModel>): RemoteKeys? {
        // Get the last page that was retrieved, that contained items
        // Form the last page, get the last item
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { repoModel ->
                // Get the remote keys of the last item retrieved
                db.keysDao().remoteKeysRepoId(repoModel.id)
            }
    }

    /**
     * LoadType.PREPEND
     */
    private suspend fun remoteKeysForFirstItem(state: PagingState<Int, RepoModel>): RemoteKeys? {
        // Get the first page that was retrieved, that contained items
        // From that first page, get the first item
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { repoModel ->
                db.keysDao().remoteKeysRepoId(repoModel.id)
            }
    }

    /**
     * LoadType.REFRESH
     */
    private suspend fun remoteKeysClosestToCurrentPosition(state: PagingState<Int, RepoModel>): RemoteKeys? {
        // The paging library is trying to load data after the anchor position
        // Get the item closest to the anchor position
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { repoId ->
                db.keysDao().remoteKeysRepoId(repoId)
            }
        }
    }
}