package com.example.playground.paging

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.example.playground.paging.api.GithubService
import com.example.playground.paging.cache.RepoDatabase
import com.example.playground.paging.data.GithubRepository

/**
 * Class that handles object creation.
 * Like this, objects can be passed as parameters in the constructors and then replaced for
 * testing, where needed.
 *
 * I like this concept
 */
object Injection {

    /**
     * Creates an instance of [GithubRepository] based on the [GithubService] and a
     * [GithubLocalCache]
     */
    private fun provideGithubRepository(context: Context): GithubRepository {
        return GithubRepository(
            GithubService.create(),
            RepoDatabase.getInstance(context)
        )
    }

    /**
     * Provides the [ViewModelProvider.Factory] that is then used to get a reference to
     * [ViewModel] objects.
     */
    fun provideViewModelFactory(context: Context): ViewModelProvider.Factory {
        return ViewModelFactory(provideGithubRepository(context))
    }
}