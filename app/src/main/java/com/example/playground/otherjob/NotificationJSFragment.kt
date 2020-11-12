package com.example.playground.otherjob

import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.example.playground.R
import com.example.playground.databinding.FragmentNotificationJobServiceBinding
import com.example.playground.util.clickAction
import com.example.playground.util.stringRes
import com.example.playground.util.toast
import timber.log.Timber

class NotificationJSFragment : Fragment() {
    private lateinit var binding: FragmentNotificationJobServiceBinding
    private val scheduler by lazy { requireActivity().getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler }

    companion object {
        private val JOB_ID = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationJobServiceBinding.inflate(inflater)
        setupUi()
        return binding.root
    }

    private fun setupUi() {
        // Button clicks
        binding.btnSchedule.clickAction { scheduleJob() }
        binding.btnCancel.clickAction { cancelJobs() }

        // Seekbar
        binding.sbOverrideValue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvDeadlineValue.text = when {
                    progress > 0 -> stringRes(requireContext(), R.string.seconds_suffix, progress)
                    else -> "Not set"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun scheduleJob() {
        val selectedNetworkOption = when (binding.rgNetworkType.checkedRadioButtonId) {
            R.id.rb_any -> JobInfo.NETWORK_TYPE_ANY
            R.id.rb_wifi -> JobInfo.NETWORK_TYPE_UNMETERED
            else -> JobInfo.NETWORK_TYPE_NONE
        }
        val jobInfo = JobInfo.Builder(
            JOB_ID,
            ComponentName(
                requireActivity().packageName,
                NotificationJobService::class.java.name
            )
        ).apply {
            setRequiredNetworkType(selectedNetworkOption)
            setRequiresDeviceIdle(binding.switchIdle.isChecked)
            setRequiresCharging(binding.switchCharging.isChecked)
            if(binding.sbOverrideValue.progress > 0) {
                setOverrideDeadline(binding.sbOverrideValue.progress * 1000L)
            }
        }

        val constraintSet = selectedNetworkOption != JobInfo.NETWORK_TYPE_NONE
                    || binding.switchIdle.isChecked || binding.switchCharging.isChecked
                    || binding.sbOverrideValue.progress > 0


        if (constraintSet) {
            scheduler.schedule(jobInfo.build())
            toast("Job Scheduled, job will run when the constraints are met", requireContext())
        } else {
            toast("Please set at least one constraint", requireContext())
        }
    }

    private fun cancelJobs() {
        scheduler.cancelAll()
        toast("Jobs cancelled", requireContext())
    }
}