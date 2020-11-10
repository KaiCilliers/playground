package com.example.playground.paging.ui

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.playground.R
import com.example.playground.paging.model.RepoModel
import com.example.playground.paging.model.UiModel
import java.lang.UnsupportedOperationException

/**
 * Adapter for the list of repositories
 * Using a PagingAdapter to replace the standard ListAdapter
 */
class ReposAdapter : PagingDataAdapter<UiModel, ViewHolder>(UIMODEL_COMPARATOR){
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val uiModel = getItem(position)
        uiModel.let {
            when (uiModel) {
                is UiModel.RepoItem -> (holder as RepoViewHolder).bind(uiModel.repo)
                is UiModel.SeparatorItem -> (holder as SeparatorViewHolder).bind(uiModel.description)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == R.layout.list_item_github_repo) {
            RepoViewHolder.create(parent)
        } else {
            SeparatorViewHolder.create(parent)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is UiModel.RepoItem -> R.layout.list_item_github_repo
            is UiModel.SeparatorItem -> R.layout.separator_view_list_item
            null -> throw UnsupportedOperationException("Unknown view")
        }
    }

    // TODO remove static instance of DiffUtil
    companion object {
        private val UIMODEL_COMPARATOR = object : DiffUtil.ItemCallback<UiModel>(){
            override fun areItemsTheSame(oldItem: UiModel, newItem: UiModel): Boolean {
                // Simply check if the two items are of the same type and then apply
                // custom comparison to each
                return (oldItem is UiModel.RepoItem && newItem is UiModel.RepoItem &&
                        oldItem.repo.fullName == newItem.repo.fullName) ||
                        (oldItem is UiModel.SeparatorItem && newItem is UiModel.SeparatorItem &&
                                oldItem.description == newItem.description)
            }

            override fun areContentsTheSame(oldItem: UiModel, newItem: UiModel): Boolean =
                oldItem == newItem
        }
    }
}