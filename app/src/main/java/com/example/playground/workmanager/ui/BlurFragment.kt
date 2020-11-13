package com.example.playground.workmanager.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.playground.R
import com.example.playground.databinding.FragmentWorkManagerBlurBinding
import com.example.playground.util.clickAction
import com.example.playground.workmanager.BlurViewModel
import com.example.playground.workmanager.util.KEY_IMAGE_URI

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

        return binding.root
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