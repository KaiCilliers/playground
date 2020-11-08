package com.example.playground.paging.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.example.playground.R
import com.example.playground.databinding.FooterListItemRepoLoadStateBinding
import com.example.playground.util.clickAction

class FooterRepoLoadStateViewHolder(
    private val binding: FooterListItemRepoLoadStateBinding,
    retry: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.retryButton.clickAction { retry.invoke() }
    }
    fun bind(loadState: LoadState) {
        if (loadState is LoadState.Error) {
            binding.errorMsg.text = loadState.error.localizedMessage
        }
        binding.progressBar.isVisible = loadState is LoadState.Loading
        binding.retryButton.isVisible = loadState !is LoadState.Loading
        binding.errorMsg.isVisible = loadState !is LoadState.Loading
    }
    companion object {
        fun create(parent: ViewGroup, retry: () -> Unit): FooterRepoLoadStateViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.footer_list_item_repo_load_state, parent, false)
            val binding = FooterListItemRepoLoadStateBinding.bind(view)
            return FooterRepoLoadStateViewHolder(binding, retry)
        }
    }
}