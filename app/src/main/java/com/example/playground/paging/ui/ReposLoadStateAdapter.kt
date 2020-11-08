package com.example.playground.paging.ui

import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter

class ReposLoadStateAdapter (private val retry: () -> Unit) : LoadStateAdapter<FooterRepoLoadStateViewHolder>() {
    override fun onBindViewHolder(holder: FooterRepoLoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): FooterRepoLoadStateViewHolder {
        return FooterRepoLoadStateViewHolder.create(parent, retry)
    }
}