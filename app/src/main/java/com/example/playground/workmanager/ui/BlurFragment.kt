package com.example.playground.workmanager.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.playground.databinding.FragmentWorkManagerBlurBinding
import com.example.playground.workmanager.BlurViewModel

class BlurFragment : Fragment() {

//    private val viewModel by lazy {  }
    private lateinit var binding: FragmentWorkManagerBlurBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWorkManagerBlurBinding.inflate(inflater)
        return binding.root
    }
}