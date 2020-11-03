package com.example.playground

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class App : Application() {
    private val applicationScope by lazy {
        CoroutineScope(Dispatchers.Default)
    }

    override fun onCreate() {
        super.onCreate()
        delayedInit()
    }

    private fun delayedInit() {
        applicationScope.launch {
            Timber.plant(Timber.DebugTree())
            createNotificationChannel()
        }
    }

    /**
     * Register a notification chanel for your app's notifications
     * This will allow all your notifications to be grouped together
     * Older APIs ignore the channel ID provided
     *
     * Needs to be created as soon as possible, thus it is called
     * in the Application class - multiple calls to create the
     * same existing channel is simply ignored
     */
    private fun createNotificationChannel() {
        // API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(
                NotificationChannel(
                    getString(R.string.channel_id),
                    getString(R.string.channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = getString(R.string.channel_description)
                }
            )
        }
    }
}