package com.example.playground.dialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import timber.log.Timber

class SharedViewModel : ViewModel() {
    private val _name = MutableLiveData<String>()
    val name: LiveData<String>
        get() {
            Timber.d("chamger tp $_name")
            return _name
        }

    fun update(name: String) {
        _name.value = name
        Timber.d("updated ${_name.value}")
    }
}