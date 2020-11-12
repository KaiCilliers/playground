package com.example.playground.otherjob

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.playground.MainActivity
import com.example.playground.R
import com.example.playground.util.stringRes

class NotificationJobService : JobService() {
    override fun onStartJob(params: JobParameters?): Boolean {
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(
                this, MainActivity::class.java
            ),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notifyManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this, stringRes(this, R.string.channel_id))
            .setContentTitle("Notification Job Service")
            .setContentText("Your Job ran to completion")
            .setContentIntent(contentPendingIntent)
            .setSmallIcon(R.drawable.ic_job_running)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
        notifyManager.notify(0, builder.build())
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }
}