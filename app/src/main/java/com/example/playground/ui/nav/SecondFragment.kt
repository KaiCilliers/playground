package com.example.playground.ui.nav

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.playground.databinding.FragmentSecondBinding
import com.example.playground.util.clickAction
import kotlinx.android.synthetic.main.fragment_second.*

class SecondFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSecondBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_go_to_three.clickAction {
            findNavController().navigate(
                SecondFragmentDirections.actionSecondFragmentToThirdFragment()
            )
        }
        btn_repos.clickAction {
            findNavController().navigate(
                SecondFragmentDirections.actionSecondFragmentToRepositoriesFragment2() // TODO rename this longass name
            )
        }
    }
}