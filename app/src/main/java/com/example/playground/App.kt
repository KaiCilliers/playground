package com.example.playground

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.collection.arraySetOf
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Annotation triggers Hilt's code generation
 * Commented out because it increases build time
 */
// @HiltAndroidApp
class App : Application(), Configuration.Provider {
    private val applicationScope by lazy {
        CoroutineScope(Dispatchers.Default)
    }

    override fun onCreate() {
        super.onCreate()
        delayedInit()
    }

    /**
     * The first time WorkManager's instance is called within
     * the app, WorkManager initializes itself using the
     * configuration returned by this function
     *
     * @return Configuration that only prints out log messages
     * with an error level when app is not in debug build and
     * at build level when it is
     */
    override fun getWorkManagerConfiguration(): Configuration {
        return if (BuildConfig.DEBUG) {
            Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                // Most notably useful changes are as follows
//                .setWorkerFactory()
//                .setJobSchedulerJobIdRange()
                .build()
        } else {
            Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.ERROR)
                .build()
        }
    }

    private fun delayedInit() {
        applicationScope.launch {
            if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
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