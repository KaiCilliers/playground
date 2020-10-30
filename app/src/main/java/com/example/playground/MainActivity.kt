package com.example.playground

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.playground.broadcast.MyReceiver
import com.example.playground.databinding.CustomToastBinding
import com.example.playground.dialog.*
import com.example.playground.service.MyService
import com.example.playground.toast.CustomToast
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_dummy.view.*
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private val KEY_TEXT_REPLY = "key_text_reply"
    private var notificationReplyIdUsedToUpdate: Int = 0
    private lateinit var binding: CustomToastBinding
    private val factory by lazy { SharedViewModelFactory() }
    private val viewModel by lazy {
        ViewModelProvider(this, factory).get(SharedViewModel::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = CustomToastBinding.inflate(layoutInflater)

        setContentView(R.layout.activity_main)
        setupClickListeners()

        viewModel.name.observe(this, Observer {
            btn_custom_dialog_input.text = it
        })

        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        remoteInput?.let {
            val reply = "${remoteInput.getCharSequence(KEY_TEXT_REPLY)}"
            btn_reply_notification.text = reply
            val repliedNotification = NotificationCompat.Builder(baseContext, getString(R.string.channel_id))
                .setSmallIcon(R.drawable.ic_sleep_active)
                .setContentText("Reply received: $reply")
                .build()
            val notificationMan = NotificationManagerCompat.from(baseContext)
            notificationMan.notify(notificationReplyIdUsedToUpdate, repliedNotification)
        }
    }

    private fun startMyService() = startService(
        Intent(baseContext, MyService::class.java)
    )

    private fun stopMyService() = stopService(
        Intent(baseContext, MyService::class.java)
    )

    private fun toastMsg(msg: String) {
        binding.tvToast.text = msg
        CustomToast(
            msg,
            applicationContext,
            binding.root
        ).show()
    }

    private fun snackbar(action: () -> Unit): Snackbar {
        val snack = Snackbar.make(
            this.findViewById(android.R.id.content),
            "I am staying here indefinitely",
            Snackbar.LENGTH_INDEFINITE
        )
        snack.setAction("Show Toast") { action() }
        return snack
    }

    private fun sendNotifiation() {
        val snoozeIntent = Intent(this, MyReceiver::class.java).apply {
            action = "MY_ACTION_SNOOZE"
            putExtra("Notification ID", 123)
        }
        val pendingSnoozeIntent = PendingIntent.getBroadcast(
            this, 0, snoozeIntent, 0
        )

        val intent = Intent(this, DummyActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        /**
         * PendingIntent is a wrapper around an Intent object
         * Primary purpose is to grant permission to a foreign
         * application to use the contained Intent as if it were
         * from your app's own process
         */
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder = NotificationCompat.Builder(this, getString(R.string.channel_id))
            .setSmallIcon(R.drawable.cupcake)
            .setContentTitle("PING PING PING")
            .setContentText(getString(R.string.notification_content))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(getString(R.string.notification_content))
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.honeycomb,
                "Call MyReceiver",
                pendingSnoozeIntent
            )
            .setAutoCancel(true)
        with(NotificationManagerCompat.from(this)) {
            notify((0..999).random(), builder.build())
        }
    }

    private fun broadcast() {
        val br = MyReceiver()
        val filter = IntentFilter(
            ConnectivityManager.EXTRA_NO_CONNECTIVITY
        ).apply {
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_TIME_TICK)
        }
        registerReceiver(br, filter)
    }

    private fun sendReplyNotification(context: Context, title: String, body: String) {
        val replyLabel = "Enter your reply here"
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).run {
            setLabel(replyLabel)
            build()
        }
        val resultIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val action = NotificationCompat.Action.Builder(
            R.drawable.marshmallow,
            "Reply",
            pendingIntent)
            .addRemoteInput(remoteInput)
            .build()

        notificationReplyIdUsedToUpdate = (0..999).random()

        val newMessageNotification = NotificationCompat.Builder(context, context.getString(R.string.channel_id))
            .setSmallIcon(R.drawable.ic_sleep_active)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(action)
        with(NotificationManagerCompat.from(context)) {
            notify(notificationReplyIdUsedToUpdate, newMessageNotification.build())
        }
    }

    private fun setupClickListeners() {
        btn_reply_notification.setOnClickListener {
            sendReplyNotification(this, "asdada", "asdasd")
        }
        btn_broadcast.setOnClickListener {
            broadcast()
            (it as Button).text = "Watch out for toasts each minute!"
            it.isEnabled = false
        }
        btn_notification.setOnClickListener {
            sendNotifiation()
        }
        btn_service_start.setOnClickListener {
            startMyService()
        }
        btn_service_stop.setOnClickListener {
            stopMyService()
        }
        btn_custom_dialog_input.setOnClickListener {
            FragmentDialogInput().show(supportFragmentManager, "TAG_CUSTOM")
        }
        btn_alert_dialog.setOnClickListener {
            CustomStockAlertDialog().show(supportFragmentManager, "TAG_CUSTOM")
        }

        btn_simple_dialog.setOnClickListener {
            FragmentCustomDialog().show(supportFragmentManager, "TAG_CUSTOM")
        }

        btn_custom_toast.setOnClickListener {
            toastMsg("This is a custom toast")
        }
        btn_snackbar.setOnClickListener {
            snackbar {
                toastMsg("Custom Toast via Snack")
            }.show()
        }
    }
}