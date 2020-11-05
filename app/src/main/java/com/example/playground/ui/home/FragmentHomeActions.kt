package com.example.playground.ui.home

import android.app.*
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.content.ComponentName
import android.graphics.drawable.Icon
import android.net.ConnectivityManager
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.preferencesKey
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.playground.MainActivity
import com.example.playground.R
import com.example.playground.broadcast.MyReceiver
import com.example.playground.databinding.CustomToastBinding
import com.example.playground.datastore.ExampleMusicPreferences
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
import com.example.playground.util.subscribe
import com.example.playground.util.toast
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class FragmentHomeAction(
    private val parent: View,
    private val fragManager: FragmentManager
) : SnackContent {

    /**
     * Using an anonymous object here just to showcase it
     */
    private val serviceSnackReceiver by lazy {
        object : BroadcastReceiver() {
            private lateinit var snackContent: SnackContent
            override fun onReceive(context: Context?, intent: Intent?) {
                context?.let { context ->
                    intent?.extras?.let { bundle ->
                        if (this::snackContent.isInitialized) {
                            snackContent.show(
                                bundle.getString(
                                    stringRes(
                                        context,
                                        R.string.intent_snack_key
                                    ), "Default text"
                                )
                            )
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
        snackbar(message, "Dismiss") { Timber.v("Cancelled job service snackbar") }.show()
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

    fun snackbar(
        message: String = "I am staying here indefinitely",
        actionName: String = "Show Toast"
        , action: () -> Unit
    ): Snackbar {
        val snack = Snackbar.make(
            parent,
            message,
            Snackbar.LENGTH_INDEFINITE
        )
        snack.setAction(actionName) { action() }
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
        // It is your responsibility to save this value
        // if you want to do anything with the notification
        // like update or remove it
        val notificationId = (0..Int.MAX_VALUE).random()

        // Notification on click destination
        val dummyIntent = Intent(context, activity::class.java).apply {
            // flags help preserve the user's expected navigation experience after they open your app via the notification
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
            .setSubText("I timeout after 10 seconds")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(stringRes(context, R.string.notification_content))
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(Notification.DEFAULT_ALL)
            .setContentIntent(pendingDummy)
            .setTimeoutAfter(10000)
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
        val remoteInput =
            RemoteInput.Builder(stringRes(context, R.string.notification_key_reply)).run {
                setLabel("Enter your reply here")
                build()
            }
        val notificationId = (0..Int.MAX_VALUE).random()
        val resultIntent = Intent(context, dest::class.java).apply {
            // This allows the object receiving the intent to check if it was started from
            // a notification and then close that notifcation
            putExtra(stringRes(context, R.string.has_notification_id_key), true)
            putExtra(stringRes(context, R.string.notification_id_key), notificationId)
            putExtra(
                stringRes(context, R.string.notification_key_reply),
                stringRes(context, R.string.notification_key_reply)
            )
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
                // TODO .setRemoteInputHistory("Remote input history?????")
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
        snackbar("Job service has stopped running (explicitly)") { Timber.v("Explicitly cancelled job service") }
    }

    fun progressNotificaiton(context: Context) {
        val builder = NotificationCompat.Builder(context, stringRes(context, R.string.channel_id)).apply {
            setContentTitle("Simulating Download")
            setSubText("I love this subtext <3")
            setContentText("Download in progress")
            setSmallIcon(R.drawable.dice_4)
            setOnlyAlertOnce(true)
            priority = NotificationCompat.PRIORITY_LOW
            // Provides system with a bit more knowledge on what
            // this notification is about so that it can handle
            // it appropriately
            setCategory(NotificationCompat.CATEGORY_STATUS)
            // Control level of detail visible in the notification
            // from the lock screen
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }

        val id = (0..Int.MAX_VALUE).random()
        // Values of the progress bar start and end value
        val max = 100
        var current = 0

        NotificationManagerCompat.from(context).apply {
            // Issue initial notification with zero progress
            builder.setProgress(max, current, false)
//            builder.setProgress(0,0,true)
            notify(id, builder.build())

            /**
             * Work that tracks progress to be done off main thread
             * Progress is shown by resetting the progress value
             * and resending a notification with same id
             *
             * The indeterminate boolean value determines if a
             * progress bar should show percentage complete
             * or not.
             *
             * true - without percentage indicator
             * false - with percentage indicator
             */
            CoroutineScope(Dispatchers.Default).launch {
//                delay(4000)
                while (current <= max) {
                    // some delay
                    delay(1500)
                    // new progress bar value
                    current += (0..30).random()
                    // added logic to prevent progress to surpass max value
                    builder.setProgress(
                        max,
                        when (current) {
                            in (max + 1)..Int.MAX_VALUE -> max
                            else -> current
                        },
                        false
                    )
                    notify(id, builder.build())
                }

                // When done, update the notification a last time to remove the progress bar
                builder.setContentText("Donwload complete")
                    // This call removes the progress bar
                    .setProgress(0, 0, false)
                notify(id, builder.build())
            }
        }

    }

    /**
     * NOTE - requires manifest permission if API 29+
     */
    fun bigTimeSensitiveFullscreenNotification(context: Context) {
        val fullScreenIntent = Intent(context, DummyActivity::class.java)
        val fullScreenPendingIntent = PendingIntent.getActivity(context, 0,
            fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context, stringRes(context, R.string.channel_id))
            .setSmallIcon(R.drawable.ic_sleep_3)
            .setContentTitle("Incoming call!")
            .setContentText("Click me to go to the call screen!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setFullScreenIntent(fullScreenPendingIntent, true)

        NotificationManagerCompat.from(context).apply {
            notify((99..9999).random(), builder.build())
        }
    }

    fun messagingStyleNotification(context: Context, activity: Activity = DummyActivity()) {
val notificationId = (444..9999).random()
        // Notification on click destination
        val dummyIntent = Intent(context, activity::class.java).apply {
            // flags help preserve the user's expected navigation experience after they open your app via the notification
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingDummy = PendingIntent.getActivity(context, 0, dummyIntent, 0)

        // Notification button action dismiss
        val dismissIntent = NotificationDismissActivity.getDismissIntent(notificationId, context)

        val timestamp1 = System.currentTimeMillis() + (62_000..62_500).random()
        val timestamp2 = System.currentTimeMillis() + (61_000..61_500).random()
        val timestamp3 = System.currentTimeMillis() + (60_000..60_500).random()
        val timestamp4 = System.currentTimeMillis()

        val me = Person.Builder()
            .setName("Me")
            .setIcon(IconCompat.createFromIcon(
                context, Icon.createWithResource(context, R.drawable.dinosaur_100)
            )).build()
        val peter = Person.Builder()
            .setName("Peter")
            .setIcon(IconCompat.createFromIcon(
                context, Icon.createWithResource(context, R.drawable.duck_100)
            )).build()
        val alex = Person.Builder()
            .setName("Alex")
            .setIcon(IconCompat.createFromIcon(
                context, Icon.createWithResource(context, R.drawable.dragon_100)
            )).build()

        val notification = NotificationCompat.Builder(
            context, stringRes(context, R.string.channel_id))
            .setSmallIcon(R.drawable.eclair)
            .setContentIntent(pendingDummy)
            .addAction(R.drawable.honeycomb, "Dismiss", dismissIntent)
            .setStyle(NotificationCompat.MessagingStyle(me)
                .setConversationTitle("Team lunch")
                .addMessage("Hi", timestamp1, me)
                .addMessage("What's up?", timestamp2, peter)
                .addMessage("Not much", timestamp3, me)
                .addMessage("How about lunch guys!?", timestamp4, alex))
            .build()
        NotificationManagerCompat.from(context).apply {
            notify(notificationId, notification)
        }
    }

    suspend fun preferencesDatasore(context: Context, owner: LifecycleOwner) {
        val example_counter = preferencesKey<Int>("example_counter")
        val datastore = context.createDataStore(
            name = "settings"
        )
        // Read from preferences datastore
        val exampleCounterFlow = datastore.data.map {preferences ->
            preferences[example_counter] ?: 0
        }

        // write to preferences datastore
        // requires to be off mian thread
        datastore.edit { settings ->
            val currentCounterValue = settings[example_counter] ?: 0
            settings[example_counter] = currentCounterValue + 1
        }

    }

    suspend fun changeSharedPreferenceDataStoreValue(id: Int, musicPreferences: ExampleMusicPreferences) {
        musicPreferences.saveLastPlayedSong(id)
        Timber.d("Saved new music preference with ID: $id")
    }

    /**
     * Proto DataStore implementation uses DataStore and
     * protocol buffers to persist typed objects to disk
     */
    suspend fun protoDatastore(context: Context) {
        // Define a schema

    }

}