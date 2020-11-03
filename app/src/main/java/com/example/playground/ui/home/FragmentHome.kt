package com.example.playground.ui.home

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.playground.databinding.CustomToastBinding
import com.example.playground.databinding.FragmentHomeBinding
import com.example.playground.dialog.SharedViewModel
import com.example.playground.dialog.SharedViewModelFactory
import com.example.playground.job.SnackContent
import com.example.playground.util.clickAction
import com.example.playground.util.snack
import com.example.playground.util.subscribe
import com.example.playground.util.toast
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

class FragmentHome : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val actions by lazy { FragmentHomeAction(parentView, parentFragmentManager) }
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

    /**
     * Register a receiver to display a snackbar
     * whenever the service gets started
     */
    override fun onResume() {
        super.onResume()
        actions.registerReceiver(requireContext())
    }

    /**
     * Unregister the broadcast receiver - some clean up
     */
    override fun onPause() {
        super.onPause()
        actions.unregisterReceiver(requireContext())
    }

    // If permission granted MainActivity will fetch contacts
    private fun requestAccess() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.READ_CONTACTS),
            1
        )
    }

    private fun setupClicks() {
        binding.apply {
            actions.apply {
                btnStartJobService.clickAction {
                    val s = requireActivity() as Activity
                    if (startJobService(s)) {
                        toast("Job service scheduled - ensure WiFi is in use", requireContext())
                        btnStopJobService.isEnabled = true
                        btnStartJobService.isEnabled = false
                    }
                }
                btnStopJobService.clickAction {
                    stopJobService()
                    toast("Job service stopped explicitly", requireContext())
                    btnStopJobService.isEnabled = false
                    btnStartJobService.isEnabled = true
                }
                btnContentProvider.clickAction {
                    // If access is granted then MainActivity will fetch contacts
                    requestAccess()
                }
                btnReplyNotification.clickAction {
                    sendReplyNotification(
                        requireContext(),
                        "title",
                        "body",
                        requireActivity()
                    )
                }
                btnBroadcast.clickAction {
                    broadcast(requireContext())
                    btnBroadcast.isEnabled = false
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