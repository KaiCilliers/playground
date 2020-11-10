package com.example.playground.paging.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.example.playground.paging.api.GithubService
import com.example.playground.paging.cache.RepoDatabase
import com.example.playground.paging.model.RepoModel

/**
 * This class will be recreated for every new query,
 * hence the amount of parameters
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
     */
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, RepoModel>
    ): MediatorResult {
        TODO("Not yet implemented")
    }
}