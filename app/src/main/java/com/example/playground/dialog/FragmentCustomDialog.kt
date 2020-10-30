package com.example.playground.dialog

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.playground.CustomToast
import com.example.playground.R
import kotlinx.android.synthetic.main.fragment_dialog_simple.view.*
import timber.log.Timber

class FragmentCustomDialog : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_dialog_simple, container, false
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
        view.btn_yes.setOnClickListener {
            Toast.makeText(requireContext(), "Alright!", Toast.LENGTH_SHORT).show()
            // Close the fragment
            dismiss()
        }
        view.btn_no.setOnClickListener {
            Toast.makeText(requireContext(), "Ok, we stay!", Toast.LENGTH_SHORT).show()
            // Close the fragment
            dismiss()
        }
    }

}