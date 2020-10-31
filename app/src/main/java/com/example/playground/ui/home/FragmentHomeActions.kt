package com.example.playground.ui.home

import android.content.Context
import android.view.View
import androidx.fragment.app.FragmentManager
import com.example.playground.databinding.CustomToastBinding
import com.example.playground.databinding.FragmentHomeBinding
import com.example.playground.dialog.CustomStockAlertDialog
import com.example.playground.dialog.FragmentCustomDialog
import com.example.playground.dialog.FragmentDialogInput
import com.example.playground.toast.CustomToast
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

class FragmentHomeAction(val binding: FragmentHomeBinding, val parent: View, val fragManager: FragmentManager) {
    fun snackbar(action: () -> Unit): Snackbar {
        val snack = Snackbar.make(
            parent,
            "I am staying here indefinitely",
            Snackbar.LENGTH_INDEFINITE
        )
        snack.setAction("Show Toast") { action() }
        return snack
    }
    fun toastMsg(msg: String, toastBinding: CustomToastBinding, context: Context) {
        Timber.d("cool")
        toastBinding.tvToast.text = msg
        CustomToast(
            msg,
            context,
            toastBinding.root
        ).show()
    }
    fun showCustomDialog(tag: String) {
        FragmentCustomDialog().show(fragManager, tag)
    }
    fun showAlertDialog(tag: String) {
        CustomStockAlertDialog().show(fragManager, tag)
    }
    fun showInputDialog(tag: String) {
        FragmentDialogInput().show(fragManager, tag)
    }
}