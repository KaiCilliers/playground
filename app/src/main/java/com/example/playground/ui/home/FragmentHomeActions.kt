package com.example.playground.ui.home

import android.app.*
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.content.ComponentName
import android.net.ConnectivityManager
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.playground.R
import com.example.playground.broadcast.MyReceiver
import com.example.playground.databinding.CustomToastBinding
import com.example.playground.dialog.CustomStockAlertDialog
import com.example.playground.dialog.FragmentCustomDialog
import com.example.playground.dialog.FragmentDialogInput
import com.example.playground.job.MyJobService
import com.example.playground.job.SnackContent
import com.example.playground.notification.NotificationDismissActivity
import com.example.playground.service.MyService
import com.example.playground.toast.CustomToast
import com.example.playground.ui.DummyActivity
import com.example.playground.util.snack
import com.example.playground.util.stringRes
import com.example.playground.util.toast
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

class FragmentHomeAction(
    private val parent: View,
    private val fragManager: FragmentManager): SnackContent {

    /**
     * Using an anonymous object here just to showcase it
     */
    private val serviceSnackReceiver by lazy {
        object : BroadcastReceiver() {
            private lateinit var snackContent: SnackContent
            override fun onReceive(context: Context?, intent: Intent?) {
                context?.let {context ->
                    intent?.extras?.let {bundle ->
                        if (this::snackContent.isInitialized) {
                            snackContent.show(bundle.getString(stringRes(context, R.string.intent_snack_key), "Default text"))
                        } else {
                            Timber.e("SnackContent not initialized")
                        }
                    }
                }
            }
            // Call to initialize SnackContent object
            fun registerInterfaceForSnackMessage(impl: SnackContent) {
                snackContent = impl
            }
        }
    }
    private lateinit var jobScheduler: JobScheduler
    private val jobID by lazy { (0..Int.MAX_VALUE).random() }

    /**
     * TODO snackbar with action to dismiss, maybe change the color a bit to test that out
     */
    override fun show(message: String) {
        snack(message, parent, Snackbar.LENGTH_INDEFINITE)
    }

    /**
     * Register receiver to be alerted when
     * my [MyJobService] has initiated its work
     */
    fun registerReceiver(context: Context) {
        val intentFilter = IntentFilter().apply {
            // filter not utilised just yet - wait a bit :)
            addAction(stringRes(context, R.string.intent_filter_service_action_key))
        }
        LocalBroadcastManager.getInstance(context).registerReceiver(
            serviceSnackReceiver, intentFilter
        )
        serviceSnackReceiver.registerInterfaceForSnackMessage(this)
    }

    /**
     * Unregister the receiver to free up resources I guess
     */
    fun unregisterReceiver(context: Context) {
        try {
            Timber.d("unregistered")
            LocalBroadcastManager.getInstance(context).unregisterReceiver(serviceSnackReceiver)
        } catch (ex: Exception) {
            Timber.e("Error unregistering ServiceReceiver\n$ex")
        }
    }

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

    fun broadcast(context: Context) {
        val filter = IntentFilter(ConnectivityManager.EXTRA_NO_CONNECTIVITY).apply {
            // Some filters *have* to be specified at runtime (like time_tick)
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_TIME_TICK)
        }
        context.registerReceiver(MyReceiver(), filter)
    }

    fun sendReplyNotification(context: Context, title: String, body: String, dest: Activity) {
        val KEY_TEXT_REPLY = "key_text_reply"
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).run {
            setLabel("Enter your reply here")
            build()
        }
        val notificationId = (0..Int.MAX_VALUE).random()
        val resultIntent = Intent(context, dest::class.java).apply {
            // This allows the object receiving the intent to check if it was started from
            // a notification and then close that notifcation
            putExtra(stringRes(context, R.string.has_notification_id_key), true)
            putExtra(stringRes(context, R.string.notification_id_key), notificationId)
            putExtra(stringRes(context, R.string.notification_key_reply), KEY_TEXT_REPLY)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val action = NotificationCompat.Action.Builder(
            R.drawable.marshmallow,
            "Reply here",
            pendingIntent
        ).addRemoteInput(remoteInput).build()

        val notification =
            NotificationCompat.Builder(context, stringRes(context, R.string.channel_id))
                .setSmallIcon(R.drawable.ic_sleep_active)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(action)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, notification.build())
        }
    }

    /**
     * @return true if started successfully to enable stop button
     */
    fun startJobService(activity: Activity): Boolean {
        Timber.d("init")
        jobScheduler = activity.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName = ComponentName(activity.baseContext, MyJobService::class.java)
        val jobInfo = JobInfo.Builder(jobID, componentName)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
            .setPeriodic(15 * 60 * 100) // minimum of 15 minutes
            .setRequiresCharging(false)
            .setPersisted(true)
            .build()

        // This does start with WiFi, just give it a few seconds to start up
        return when (jobScheduler.schedule(jobInfo)) {
            JobScheduler.RESULT_SUCCESS -> {
                Timber.d("Job scheduled successfully")
                true
            }
            else -> {
                Timber.d("Job could not be scheduled")
                false
            }
        }
    }

    fun stopJobService() {
        jobScheduler.cancel(jobID)
    }
}