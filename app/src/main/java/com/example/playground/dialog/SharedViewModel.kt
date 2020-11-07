package com.example.playground.dialog

import android.content.ContentResolver
import android.provider.ContactsContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.StringBuilder

class SharedViewModel : ViewModel() {

    val test = "peep"

    private val _contacts = MutableLiveData<String>()
    val contacts: LiveData<String>
        get() {
            Timber.d("Updated contacts")
            return  _contacts
        }

    private val _name = MutableLiveData<String>()
    val name: LiveData<String>
        get() {
            Timber.d("updated name to $_name")
            return _name
        }

    fun update(name: String) {
        _name.value = name
        Timber.d("updated ${_name.value}")
    }

    fun fetchContacts(contentResolver: ContentResolver) {
        viewModelScope.launch {
            val columnNames = arrayOf(
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                ContactsContract.Contacts.CONTACT_STATUS,
                ContactsContract.Contacts.HAS_PHONE_NUMBER
            )

            val contentResolver = contentResolver
            val cursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                columnNames, null, null, null
            )

            var counter = 0
            cursor?.let {
                if (cursor.count > 0) {
                    val result = StringBuilder()
                    while (cursor.moveToNext()) {
                        counter++
                        result.apply {
                            append(cursor.getString(0) + ", ")
                            append(cursor.getString(1) + ", ")
                            append(cursor.getString(2) + "\n")
                        }
                    }
                    _contacts.value = result.toString()
                }
            }
        }
    }
}