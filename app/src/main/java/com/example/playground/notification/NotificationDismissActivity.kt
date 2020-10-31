package com.example.playground.notification

import android.app.Activity
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle


/**
 * Only function is to close a notification via
 * a dismiss button option on the notification
 *
 * Attributes set in manifest of this activity
 * is required to prevent SystemUI from focusing
 * to a back stack
 *
 * Also possible to do it via a Broadcast Receiver
 * https://www.semicolonworld.com/question/48769/how-to-dismiss-notification-after-action-has-been-clicked
 */
class NotificationDismissActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        intent.extras?.let {
            manager.cancel(it.getInt(NOTIFICATION_ID, -1))
        }
        // this will can onDestroy() immediately
        finish()
    }
    companion object {
        val NOTIFICATION_ID = "NOTIFICATION_ID"
        fun getDismissIntent(notificationId: Int, context: Context): PendingIntent {
            val intent = Intent(context, NotificationDismissActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(NOTIFICATION_ID, notificationId)
            }
            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        }
    }
}