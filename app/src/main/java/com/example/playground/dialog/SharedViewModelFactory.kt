package com.example.playground.dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class SharedViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(SharedViewModel::class.java))
            return SharedViewModel() as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}