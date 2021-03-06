package com.example.playground.util

import android.app.Application
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.lifecycle.*
import com.example.playground.workmanager.BlurViewModel
import com.google.android.material.snackbar.Snackbar

fun toast(msg: String, context: Context) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
fun toast(msg: String, context: Context, duration: Int) = Toast.makeText(context, msg, duration).show()
fun snack(msg: String, view: View) = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show()
fun snack(msg: String, view: View, duration: Int) = Snackbar.make(view, msg, duration).show()
inline fun View.clickAction(crossinline action: () -> Unit) = setOnClickListener { action() }
inline fun <T> LiveData<T>.subscribe(owner: LifecycleOwner, crossinline action: (T) -> Unit) = observe(owner, Observer { action(it) })
fun stringRes(context: Context, id: Int) = context.getString(id)
fun stringRes(context: Context, id: Int, vararg formatArgs: Any) = context.getString(id, *formatArgs)