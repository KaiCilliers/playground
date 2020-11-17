package com.example.playground.testing.util

import androidx.lifecycle.Observer

/**
 * AN [Observer] for [TodoEvent]s, simplifying the pattern of checking if the [TodoEvent]'s content has
 * already been handled
 *
 * [onEventUnhandledContent] is *only* called if the [TodoEvent]'s contents has not been handled
 */
class TodoEventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<TodoEvent<T>> {
    override fun onChanged(event: TodoEvent<T>?) {
        event?.getContentIfNotHandled()?.let {
            onEventUnhandledContent(it)
        }
    }
}