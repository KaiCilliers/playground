package com.example.playground

import android.content.Context
import android.view.View
import android.widget.Toast

fun toast(msg: String, context: Context) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()