package com.example.playground.workmanager.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import com.bumptech.glide.Glide
import com.example.playground.R
import com.example.playground.databinding.FragmentWorkManagerBlurBinding
import com.example.playground.util.clickAction
import com.example.playground.workmanager.BlurViewModel
import com.example.playground.workmanager.util.KEY_IMAGE_URI
import timber.log.Timber

class BlurFragment : Fragment() {

    private val viewModel: BlurViewModel by viewModels()
    private lateinit var binding: FragmentWorkManagerBlurBinding
    private val blurLevel: Int
        get() =
            when (binding.rgBlur.checkedRadioButtonId) {
                R.id.rb_blur_lv_2 -> 2
                R.id.rb_blur_lv_3 -> 3
                else -> 1
            }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWorkManagerBlurBinding.inflate(inflater)

        // Image uri should be stored in the ViewModel; put it there then display
        val imageUriExtra = requireArguments().getString(KEY_IMAGE_URI)
        viewModel.setImageUri(imageUriExtra)
        viewModel.imageUri?.let { imageUri ->
            Glide.with(requireActivity()).load(imageUri).into(binding.ivMain)
        }

        binding.btnGo.clickAction { viewModel.applyBlur(blurLevel) }

        // Setup view output image file button
        binding.btnSeeFile.clickAction {
            viewModel.outputUri?.let { currentUri ->
                val actionView = Intent(Intent.ACTION_VIEW, currentUri)
                actionView.resolveActivity(requireActivity().packageManager)?.run {
                    startActivity(actionView)
                }
            }
        }

        binding.btnCancel.clickAction {
            viewModel.cancelWork()
        }

        viewModel.outputWorkInfos.observe(requireActivity(), workInfosObserver())

        return binding.root
    }

    private fun workInfosObserver(): Observer<List<WorkInfo>> {
        return Observer { listOfWorkInfo ->
            // Note that these next few lines grab a single WorkInfo if it exists
            // This code could be in a Transformation in the ViewModel; they are included here
            // so that the entire process of displaying a WorkInfo is in one location.
            Timber.d("$listOfWorkInfo")
            // If there are no matching work info, do nothing
            if (listOfWorkInfo.isNullOrEmpty()) {
                return@Observer
            }

            // We only care about the one output status
            // Every continuation has only one worker tagged TAG_OUTPUT
            val workInfo = listOfWorkInfo[0]

            if (workInfo.state.isFinished) {
                showWorkFinished()

                // Normally this processing, which is not directly related to drawing views on
                // screen would be in the ViewModel. For simplicity we are keeping it here.
                val outputImageUri = workInfo.outputData.getString(KEY_IMAGE_URI)

                // If there is an output file show "See File" button
                if (!outputImageUri.isNullOrEmpty()) {
                    viewModel.setOutputUri(outputImageUri)
                    binding.btnSeeFile.visibility = View.VISIBLE
                }
            } else {
                showWorkInProgress()
            }
        }
    }

    /**
     * Shows and hides views for when the Activity is processing an image
     */
    private fun showWorkInProgress() {
        with(binding) {
            pbLoading.visibility = View.VISIBLE
            btnCancel.visibility = View.VISIBLE
            btnGo.visibility = View.GONE
            btnSeeFile.visibility = View.GONE
        }
    }
    /**
     * Shows and hides views for when the Activity is done processing an image
     */
    private fun showWorkFinished() {
        with(binding) {
            pbLoading.visibility = View.GONE
            btnCancel.visibility = View.GONE
            btnGo.visibility = View.VISIBLE
        }
    }
}