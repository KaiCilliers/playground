package com.example.playground.paging.ui

import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.playground.R
import com.example.playground.databinding.FragmentRepositoriesBinding
import com.example.playground.paging.Injection
import com.example.playground.paging.SearchRepoViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 */
class RepositoriesFragment : Fragment() {

    // TODO place your own flavour
    private lateinit var binding: FragmentRepositoriesBinding
    private lateinit var viewModel: SearchRepoViewModel
    private val adapter = ReposAdapter()
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRepositoriesBinding.inflate(layoutInflater)

        // get the view model
        viewModel = ViewModelProvider(this, Injection.provideViewModelFactory())
            .get(SearchRepoViewModel::class.java)

        // add dividers between RecyclerView's row items
        val decoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        binding.rcRepositories.addItemDecoration(decoration)

        binding.rcRepositories.adapter = adapter.withLoadStateHeaderAndFooter(
            // add it to header as well for when you want to dispose of
            // previous pages and need to reload them
            header = ReposLoadStateAdapter { adapter.retry() },
            footer = ReposLoadStateAdapter { adapter.retry() }
        )

        val query = savedInstanceState?.getString(LAST_SEARCH_QUERY) ?: DEFAULT_QUERY
        search(query)
        initSearch(query)

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(LAST_SEARCH_QUERY, binding.etSearchRepo.text.trim().toString())
    }

    /**
     * Responsible for the following
     *
     * Cancel previous search job
     * Launch new job in lifecycleScope
     * Call viewModel.searchRepo
     * Collect the PagingData result
     * Pass the PagingData to the ReposAdapter by calling adapter.submitData(pagingData)
     */
    private fun search(query: String) {
        // Make sure to cancel previous job before creating a new one
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            viewModel.searchRepo(query).collectLatest {
                adapter.submitData(it)
            }
        }
    }

    private fun updateRepoListFromInput() {
        binding.etSearchRepo.text.trim().let {
            if (it.isNotEmpty()) {
                // You can scroll to top with simply this,
                // but we'd rather reset when adapter gets updated
//                binding.list.scrollToPosition(0)
                search("$it")
            }
        }
    }

    private fun initSearch(query: String) {
        binding.etSearchRepo.setText(query)

        binding.etSearchRepo.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                updateRepoListFromInput()
                true
            } else {
                false
            }
        }
        binding.etSearchRepo.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                updateRepoListFromInput()
                true
            } else {
                false
            }
        }
        /**
         * New Addition
         * @desc Scroll to top when the list is refreshed from the network
         *
         * Uses loadStateFlow API
         * This flow emits every time there's a change in the load state via
         * a CombinedLoadState object. CombinedLoadStates allows us to get
         * the load state for the 3 different types of load operations
         *
         * CombinedLoadStates.refresh - represents the load state for loading the PagingData for the first time
         * CombinedLoadStates.prepend represents the load state for loading data at the start of the list
         * CombinedLoadStates.append - represents the load state fot loading data at the end of the list
         */
        lifecycleScope.launch {
            adapter.loadStateFlow
                // Only emit when REFRESH LoadState changes
                .distinctUntilChangedBy { it.refresh }
                // Only react to cases where REFRESH completes i.e., NotLoading
                .filter { it.refresh is LoadState.NotLoading }
                // Receive/collect an emit and run your code
                .collect { binding.rcRepositories.scrollToPosition(0) }
        }
    }


    // TODO unused
    private fun showEmptyList(show: Boolean) {
        if (show) {
            binding.tvEmptyList.visibility = View.VISIBLE
            binding.rcRepositories.visibility = View.GONE
        } else {
            binding.tvEmptyList.visibility = View.GONE
            binding.rcRepositories.visibility = View.VISIBLE
        }
    }

    companion object {
        private const val LAST_SEARCH_QUERY: String = "last_search_query"
        private const val DEFAULT_QUERY = "Android"
    }

}