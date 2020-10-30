package com.example.playground.toast

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.Toast

class CustomToast(private val msg: String, private val context: Context, private val views: View) {
    fun show() {
        with(
            Toast.makeText(context, msg, Toast.LENGTH_SHORT)
        ) {
            view = views
            setGravity(Gravity.CENTER_VERTICAL, 0, 700)
            show()
        }
    }
}