package com.example.playground.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.playground.R
import com.example.playground.util.clickAction
import kotlinx.android.synthetic.main.fragment_dialog_input.view.*

class FragmentDialogInput : DialogFragment() {
    private val factory by lazy { SharedViewModelFactory() }
    private val viewModel by lazy {
        ViewModelProvider(requireActivity(), factory).get(SharedViewModel::class.java)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_dialog_input, container, false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners(view)
    }

    /**
     * Stretches Dialog horizontally
     */
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setupClickListeners(view: View) {
        view.btn_submit.clickAction {
            viewModel.update("${view.et_name.text}")
            dismiss()
        }
    }

}