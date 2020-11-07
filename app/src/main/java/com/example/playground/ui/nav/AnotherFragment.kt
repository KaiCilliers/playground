package com.example.playground.ui.nav

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.playground.R
import com.example.playground.databinding.FragmentAnotherBinding
import com.example.playground.util.clickAction
import kotlinx.android.synthetic.main.fragment_another.*

class AnotherFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentAnotherBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_nested_graph.clickAction {
            findNavController().navigate(
                R.id.action_global_navigation
            )
        }
    }
}