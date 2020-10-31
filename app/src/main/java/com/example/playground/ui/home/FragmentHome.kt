package com.example.playground.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.playground.databinding.CustomToastBinding
import com.example.playground.databinding.FragmentHomeBinding
import com.example.playground.dialog.*
import com.example.playground.util.clickAction
import com.example.playground.util.subscribe
import timber.log.Timber

class FragmentHome : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val actions by lazy { FragmentHomeAction(binding, parentView, parentFragmentManager) }
    private val parentView by lazy { requireActivity().findViewById<View>(android.R.id.content) }
    private lateinit var fragInflater: LayoutInflater
    private val factory by lazy { SharedViewModelFactory() }
    private val sharedViewModel by lazy { ViewModelProvider(requireActivity(), factory).get(SharedViewModel::class.java) }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragInflater = inflater
        binding = FragmentHomeBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner

        sharedViewModel.name.subscribe(viewLifecycleOwner) {
            binding.btnCustomDialogInput.text = it
        }

        setupClicks()
        return binding.root
    }

    private fun setupClicks() {
        binding.apply {
            actions.apply {
                btnContentProvider
                btnReplyNotification
                btnBroadcast.clickAction {

                }
                btnNotification.clickAction {
                    sendNotification(requireContext())
                }
                btnServiceStart.clickAction {
                    startService(requireContext())
                }
                btnServiceStop.clickAction {
                    stopService(requireContext())
                }
                btnCustomDialogInput.clickAction {
                    showInputDialog("tag")
                }
                btnAlertDialog.clickAction {
                    showAlertDialog("tag")
                }
                btnSimpleDialog.clickAction {
                    showCustomDialog("tag")
                }
                btnCustomToast.clickAction {
                    toastMsg(
                        "This is a custom Toast",
                        CustomToastBinding.inflate(fragInflater),
                        requireContext()
                    )
                }
                btnSnackbar.clickAction {
                    Timber.d("adadasdasd")
                    snackbar {
                        toastMsg(
                            "Custom toast via snackbar click",
                            CustomToastBinding.inflate(fragInflater),
                            requireContext()
                        )
                    }.show()
                }
            }
        }
    }
}