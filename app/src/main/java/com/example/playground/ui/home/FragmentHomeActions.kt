package com.example.playground.ui.home

import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.FragmentManager
import com.example.playground.R
import com.example.playground.broadcast.MyReceiver
import com.example.playground.databinding.CustomToastBinding
import com.example.playground.databinding.FragmentHomeBinding
import com.example.playground.dialog.CustomStockAlertDialog
import com.example.playground.dialog.FragmentCustomDialog
import com.example.playground.dialog.FragmentDialogInput
import com.example.playground.notification.NotificationDismissActivity
import com.example.playground.service.MyService
import com.example.playground.toast.CustomToast
import com.example.playground.ui.DummyActivity
import com.example.playground.util.stringRes
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

class FragmentHomeAction(
    val binding: FragmentHomeBinding,
    val parent: View,
    val fragManager: FragmentManager
) {
    fun snackbar(action: () -> Unit): Snackbar {
        val snack = Snackbar.make(
            parent,
            "I am staying here indefinitely",
            Snackbar.LENGTH_INDEFINITE
        )
        snack.setAction("Show Toast") { action() }
        return snack
    }

    fun toastMsg(msg: String, toastBinding: CustomToastBinding, context: Context) {
        Timber.d("cool")
        toastBinding.tvToast.text = msg
        CustomToast(
            msg,
            context,
            toastBinding.root
        ).show()
    }

    fun showCustomDialog(tag: String) {
        FragmentCustomDialog().show(fragManager, tag)
    }

    fun showAlertDialog(tag: String) {
        CustomStockAlertDialog().show(fragManager, tag)
    }

    fun showInputDialog(tag: String) {
        FragmentDialogInput().show(fragManager, tag)
    }

    fun startService(context: Context, service: Service = MyService()) =
        context.startService(Intent(context, service::class.java))

    fun stopService(context: Context, service: Service = MyService()) =
        context.stopService(Intent(context, service::class.java))

    /**
     * PendingIntent is a wrapper around an Intent object
     * Primary purpose is to grant permission to a foreign
     * application to use the contained Intent as if it were
     * from your app's own process
     *
     * TODO extract to smaller private functions located at
     * bottom of file to keep top file with public functions
     * neater
     */
    fun sendNotification(
        context: Context,
        activity: Activity = DummyActivity(),
        receiver: BroadcastReceiver = MyReceiver()
    ) {
        val notificationId = (0..Int.MAX_VALUE).random()

        // Notification on click destination
        val dummyIntent = Intent(context, activity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingDummy = PendingIntent.getActivity(context, 0, dummyIntent, 0)

        // Notification button click action
        val actionIntent = Intent(context, receiver::class.java).apply {
            action = "CUSTOM_ACTION_STRING"
            // This allows the object receiving the intent to check if it was started from
            // a notification and then close that notifcation
            putExtra(stringRes(context, R.string.has_notification_id_key), true)
            putExtra(stringRes(context, R.string.notification_id_key), notificationId)
        }
        val pendingAction = PendingIntent.getBroadcast(
            context, 0, actionIntent, PendingIntent.FLAG_CANCEL_CURRENT
        )

        // Notification button action dismiss
        val dismissIntent = NotificationDismissActivity.getDismissIntent(notificationId, context)

        // Building the notification
        val notification = NotificationCompat.Builder(
            context, stringRes(context, R.string.channel_id)
        ).setSmallIcon(R.drawable.cupcake)
            .setContentTitle("Cupcakes are ready!")
            .setContentText(stringRes(context, R.string.notification_content))
            .setSubText("This is some sub text")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(stringRes(context, R.string.notification_content))
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(Notification.DEFAULT_ALL)
            .setContentIntent(pendingDummy)
            .addAction(R.drawable.ic_sleep_1, "Dismiss", dismissIntent)
            .addAction(R.drawable.ic_sleep_active, "Start Receiver", pendingAction)
            .setAutoCancel(true)

        // Launch notification
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, notification.build())
        }
    }
}