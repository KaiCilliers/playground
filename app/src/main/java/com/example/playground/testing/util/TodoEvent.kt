package com.example.playground.testing.util

/**
 * Wrapper class for data that is exposed via LiveData that represents an event
 */
open class TodoEvent<out T>(private val content: T) {
    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Returns the content and prevents its use again
     */
    fun getContentIfNotHandled(): T? {
        return  if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled
     */
    fun peekContent(): T = content
}