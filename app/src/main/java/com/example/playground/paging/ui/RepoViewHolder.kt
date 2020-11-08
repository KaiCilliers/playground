package com.example.playground.paging.ui

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.playground.R
import com.example.playground.paging.RepoModel
import com.example.playground.util.clickAction
import kotlinx.android.synthetic.main.list_item_github_repo.view.*

/**
 * View Holder for a [RepoModel] RecyclerView list item
 * TODO impl databinding
 */
class RepoViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val name: TextView = view.repo_name
    private val description: TextView = view.repo_description
    private val stars: TextView = view.repo_stars
    private val language: TextView = view.repo_language
    private val forks: TextView = view.repo_forks

    private var repo: RepoModel? = null

    init {
        view.clickAction {
            repo?.url?.let { url ->
                view.context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW, Uri.parse(url)
                    )
                )
            }
        }
    }
    fun bind(repo: RepoModel?) {
        if(repo == null) { // list item indicates data is loading
            val resources = itemView.resources
            name.text = resources.getString(R.string.loading)
            description.visibility = View.GONE
            language.visibility = View.GONE
            stars.text = resources.getString(R.string.unknown)
            forks.text = resources.getString(R.string.unknown)
        } else {
            showRepoData(repo)
        }
    }

    /**
     * Actual place where data gets bound to view
     */
    private fun showRepoData(repo: RepoModel) {
        this.repo = repo

        name.text = repo.fullName
        stars.text = "${repo.stars}"
        forks.text = "${repo.forks}"

        // if description is missing, hide
        var descVisibility = View.GONE
        if(repo.description != null) {
            description.text = repo.description
            descVisibility = View.VISIBLE
        }
        description.visibility = descVisibility

        // if the language is missing, hide label and value
        var langVisibility = View.GONE
        if(!repo.language.isNullOrEmpty()) {
            val resources = itemView.resources
            language.text = resources.getString(R.string.language, repo.language)
            langVisibility = View.VISIBLE
        }
        language.visibility = langVisibility
    }

    /**
     * TODO remove static object
     */
    companion object {
        fun create(parent: ViewGroup): RepoViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_github_repo, parent, false)
            return RepoViewHolder(view)
        }
    }
}