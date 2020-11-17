package com.example.playground.testing.util

import com.example.playground.testing.data.Result

/**
 * `true` if [Result] is of type [Success] & holds non-null [Success.data].
 */
val Result<*>.succeeded
    get() = this is Result.Success && data != null