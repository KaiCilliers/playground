package com.example.playground.broadcast

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.playground.R
import com.example.playground.util.stringRes
import com.example.playground.util.toast
import timber.log.Timber
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MyReceiver : BroadcastReceiver() {
    /**
     * System can kill the process at any time
     * without finishing the operation.
     * Rather use a JobScheduler
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            intent?.extras?.let {bundle ->
                if(bundle.getBoolean(stringRes(context, R.string.has_notification_id_key), false)) {
                    val notificationID = bundle.getInt(stringRes(context, R.string.notification_id_key), -1)
                    Timber.d("Got notification ID of: $notificationID")
                    NotificationManagerCompat.from(context).cancel(notificationID)
                }
            }
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    Timber.v("Screen is off")
                    sendNotification(context, "Screen is off", "You switched the screen off")
                }
                Intent.ACTION_SCREEN_ON -> {
                    sendNotification(context, "Screen is on", "You switched the screen on")
                    Timber.v("Screen is on")
                }
                Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                    val toggledOn = intent.extras?.getBoolean("state", false)
                    Timber.v(
                        "Extras: ${intent.extras}\n" +
                                "Possible value: $toggledOn"
                    )
                    toggledOn?.let {
                        sendNotification(
                            context, "Airplane mode Toggled",
                            "Airplane mode is now ${if (toggledOn) "on" else "off"}"
                        )
                    }
                    Timber.v("PLANES")
                }
                Intent.ACTION_TIME_TICK -> {
                    val time = SimpleDateFormat("HH:mm:ss").format(Date())
                    sendNotification(context, time, "A minute has passed")
                    Timber.v("time changed... to $time")
                    toast("Time is now $time - Try toggling airplane mode", context, Toast.LENGTH_LONG)
                }
            }
        }
    }
    // TODO heads up display notification
    private fun sendNotification(context: Context, title: String, body: String) {
        val privateNotification = NotificationCompat.Builder(context, stringRes(context, R.string.channel_id))
            .setSmallIcon(R.drawable.ic_sleep_3)
            .setContentTitle("Shhhh!")
            .setContentText("You have a secret message")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setDefaults(Notification.DEFAULT_ALL)
        val builder = NotificationCompat.Builder(context, stringRes(context, R.string.channel_id))
            .setSmallIcon(R.drawable.ic_sleep_5)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(Notification.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setPublicVersion(privateNotification.build())
            .setTimeoutAfter(4000)
        with(NotificationManagerCompat.from(context)) {
            notify((1..Int.MAX_VALUE).random(), builder.build())
        }
    }
}
