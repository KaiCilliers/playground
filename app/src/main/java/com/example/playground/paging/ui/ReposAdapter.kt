package com.example.playground.paging.ui

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.playground.paging.RepoModel

/**
 * Adapter for the list of repositories
 * Using a PagingAdapter to replace the standard ListAdapter
 */
class ReposAdapter : PagingDataAdapter<RepoModel, RecyclerView.ViewHolder>(REPO_COMPARATOR){
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val repoItem = getItem(position)
        repoItem?.let {
            (holder as RepoViewHolder).bind(it)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return RepoViewHolder.create(parent)
    }

    // TODO remove static instance of DiffUtil
    companion object {
        private val REPO_COMPARATOR = object : DiffUtil.ItemCallback<RepoModel>(){
            override fun areItemsTheSame(oldItem: RepoModel, newItem: RepoModel): Boolean =
                oldItem.fullName == newItem.fullName

            override fun areContentsTheSame(oldItem: RepoModel, newItem: RepoModel): Boolean =
                oldItem == newItem
        }
    }
}