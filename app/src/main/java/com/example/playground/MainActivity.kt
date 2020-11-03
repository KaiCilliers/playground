package com.example.playground

import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.playground.databinding.CustomToastBinding
import com.example.playground.dialog.SharedViewModel
import com.example.playground.dialog.SharedViewModelFactory
import com.example.playground.util.snack
import com.example.playground.util.subscribe
import com.example.playground.util.toast
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private val jobScheduler: JobScheduler by lazy { getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler }
    private var notificationReplyIdUsedToUpdate: Int = 0
    private lateinit var binding: CustomToastBinding
    private val factory by lazy { SharedViewModelFactory() }
    private val viewModel by lazy {
        ViewModelProvider(this, factory).get(SharedViewModel::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = CustomToastBinding.inflate(layoutInflater)

        viewModel.contacts.subscribe(this) {
            snack("Loaded contacts", findViewById<View>(android.R.id.content))
            Timber.d("Contacts: $it")
        }

        setContentView(R.layout.activity_main)

        handleAnyInputNotification()
    }

    private fun handleAnyInputNotification() {
        // Handle input from input Notification
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        remoteInput?.let {
            val replyText = "${remoteInput.getCharSequence(getString(R.string.notification_key_reply))}"
            val repliedNotification =
                NotificationCompat.Builder(baseContext, getString(R.string.channel_id))
                    .setSmallIcon(R.drawable.ic_sleep_active)
                    .setContentText("Reply received: $replyText")
                    .build()
            val notificationMan = NotificationManagerCompat.from(baseContext)
            notificationMan.notify(notificationReplyIdUsedToUpdate, repliedNotification)
        }
        // Cancel input notification
        intent.extras?.let {
            if(it.getBoolean(getString(R.string.has_notification_id_key), false)) {
                val notificationId = it.getInt(getString(R.string.notification_id_key), -1)
                NotificationManagerCompat.from(baseContext).cancel(notificationId)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            1 -> {
                // If result is cancelled, the result arrays are empty
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    viewModel.fetchContacts(contentResolver)
                    toast("Permission to read contacts granted", baseContext)
                } else {
                    // permission denied - Disable the functionality that depends on this permission
                    toast("Permission to read contacts denied", baseContext)
                }
                return
            }
        }
    }
}
