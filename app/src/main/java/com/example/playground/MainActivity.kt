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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {
    private val jobScheduler: JobScheduler by lazy { getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler }
    private var notificationReplyIdUsedToUpdate: Int = 0
    private lateinit var binding: CustomToastBinding
    private val factory by lazy { SharedViewModelFactory() }
    private val viewModel by lazy {
        ViewModelProvider(this, factory).get(SharedViewModel::class.java)
    }

    // CPU consuming execution
    private val mainScopeDef by lazy { CoroutineScope(Dispatchers.Default) }

    // UI updating code
    private val mainScopeMain by lazy { CoroutineScope(Dispatchers.Main) }
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
    /**
     * A notification supports a reply feature where
     * it can send a reply in the notification after
     * typing and hitting send.
     *
     * The send button fires an intent to launch
     * [MainActivity], thus this method is called at
     * onCreate.
     *
     * This method captures and handles the message
     * received via the notification and it handles
     * what to do with the notification further
     */
    private fun handleAnyInputNotification() {
        // Handle input from input Notification
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        remoteInput?.let {
            val replyText =
                "${remoteInput.getCharSequence(getString(R.string.notification_key_reply))}"
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
            if (it.getBoolean(getString(R.string.has_notification_id_key), false)) {
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                // If result is cancelled, the result arrays are empty
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
    private fun someFlowOperations() {
        /**
         * ONE
         */
        runBlocking {
            withTimeoutOrNull(250) { // Timeout after 250ms
                simple().collect { value -> println(value) }
            }
            println("Done")
        }
        /**
         * TWO
         */
        runBlocking {
            (1..5).asFlow()
                .filter { Timber.d("Filter $it"); it % 2 == 0 }
                .map { Timber.d("Map $it"); "string $it" }
                .collect { Timber.d("Collect $it") }
        }
        /**
         * THREE
         */
        runBlocking {
            val time = measureTimeMillis {
                simple().collect {
                    delay(300) // pretend we are processing it for 300 ms
                    Timber.v("$it")
                }
            }
            Timber.d("Collected in $time ms without buffer")
        }
        runBlocking {
            val time = measureTimeMillis {
                simple()
                    // Faster because you only wait the 100ms ONCE
                    .buffer() // buffer emissions, don't wait
                    .collect {
                        delay(300)
                        Timber.v("$it")
                    }
            }
            Timber.v("Collected in $time ms with buffer")
        }
        /**
         * FOUR - zip() combines corresponding values of two flows
         */
        runBlocking {
            val nums = (1..3).asFlow()
            val strs = flowOf("one", "two", "three")
            nums.zip(strs) { a, b -> "$a -> $b" }.collect { Timber.v("$it") }
        }
        /**
         * FIVE - Conflation is used to skip intermediate values when a
         * collector is too slow to process them. Good when a flow represents
         * partial results of the operation or operation status updates which
         * means it may not be necessary to process each value, but instead only
         * the most recent ones
         */
        runBlocking {
            val time = measureTimeMillis {
                simple()
                    .conflate() // conflate emissions, don't process each one
                    .collect { value ->
                        delay(300) // pretend we are processing it for 300 ms
                        println(value)
                    }
            }
            Timber.v("Conflate - Collected in $time ms")
        }
        /**
         * SIX - Combine
         */
        runBlocking {
            Timber.d("COMBINE")
            val nums = (1..3).asFlow().onEach { delay(300) } // numbers 1..3 every 300 ms
            val strs = flowOf("one", "two", "three").onEach { delay(400) } // strings every 400 ms
            val startTime = System.currentTimeMillis() // remember the start time
            nums.zip(strs) { a, b -> "$a -> $b" } // compose a single string with "zip"
                .collect { value -> // collect and print
                    println("$value at ${System.currentTimeMillis() - startTime} ms from start zip")
                }
        }
        runBlocking {
            val nums = (1..3).asFlow().onEach { delay(300) } // numbers 1..3 every 300 ms
            val strs = flowOf("one", "two", "three").onEach { delay(400) } // strings every 400 ms
            val startTime = System.currentTimeMillis() // remember the start time
            nums.combine(strs) { a, b -> "$a -> $b" } // compose a single string with "combine"
                .collect { value -> // collect and print
                    println("$value at ${System.currentTimeMillis() - startTime} ms from start combine")
                }
        }
    }

    private fun simple(): Flow<Int> = flow {
        for (i in 1..4) {
            delay(100) // pretend we are asynchronously waiting 100 ms
            emit(i) // emit next value
        }
    }
}
