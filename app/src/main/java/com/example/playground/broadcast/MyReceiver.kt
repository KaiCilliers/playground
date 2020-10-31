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
                Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                    val toggledOn = intent.extras?.getBoolean("state", false)
                    Timber.d(
                        "Extras: ${intent.extras}\n" +
                                "Possible value: $toggledOn"
                    )
                    toggledOn?.let {
                        sendNotification(
                            context, "Airplane mode Toggled",
                            "Airplane mode is now ${if (toggledOn) "on" else "off"}"
                        )
                    }
                    Timber.d("PLANES")
                }
                Intent.ACTION_TIME_TICK -> {
                    Timber.d("time changed...")
                    toast("Time Changed - Try toggling airplane mode", context, Toast.LENGTH_LONG)
                }
            }
        }
    }
    private fun sendNotification(context: Context, title: String, body: String) {
        val builder = NotificationCompat.Builder(context, stringRes(context, R.string.channel_id))
            .setSmallIcon(R.drawable.ic_sleep_5)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(context)) {
            notify((1..Int.MAX_VALUE).random(), builder.build())
        }
    }
}