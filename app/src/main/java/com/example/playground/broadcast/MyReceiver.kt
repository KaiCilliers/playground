package com.example.playground.broadcast

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.playground.DummyActivity
import com.example.playground.R
import com.example.playground.util.snack
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
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    toast("Broadcast Receiver - screen is off", it)
                }
                Intent.ACTION_SCREEN_ON -> {
                    toast("Broadcast Receiver - screen is on", it)
                }
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
                    toast("Time Changed - Try toggling airplane mode", context, Toast.LENGTH_LONG)
                }
            }

            Timber.d("Broadcast Receiver with intent: $intent \n and context: $context")
        }
    }

    private fun sendNotification(context: Context, title: String, body: String) {
        val builder = NotificationCompat.Builder(context, context.getString(R.string.channel_id))
            .setSmallIcon(R.drawable.ic_sleep_5)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(context)) {
            notify((1..999).random(), builder.build())
        }
    }
}