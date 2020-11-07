package com.example.playground.job

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.playground.R
import com.example.playground.util.snack
import com.example.playground.util.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber


class MyJobService : JobService() {
    private val jobScope by lazy { CoroutineScope(Dispatchers.Default) }

    /**
     * @return true means restart if killed
     * @return false means do not restart if killed
     * TODO call a snackbar to indicate the service has stopped
     */
    override fun onStopJob(params: JobParameters?): Boolean {
        Timber.d("onStopJob called")

        val intent = Intent(getString(R.string.intent_filter_service_action_key))
        intent.putExtra(getString(R.string.intent_snack_key), "Job service has stopped running")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        return true
    }

    /**
     * @return true means long duration task
     * @return false means short duration task
     */
    override fun onStartJob(params: JobParameters?): Boolean {
        Timber.d("onStartJob called and it should restart the service")
        doBackgroundWork(params)

        val intent = Intent(getString(R.string.intent_filter_service_action_key))
        intent.putExtra(getString(R.string.intent_snack_key), "Job service has started running")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        return true
    }

    // Changed to run and finish and be rescheduled
    private fun doBackgroundWork(params: JobParameters?) {
        var counter = 0
        var randomInt = 0
        jobScope.launch {
            while (randomInt < 8) {
                delay(500)
                counter++
                randomInt = (0..8).random()
                Timber.d("Random value: $randomInt")
            }
            // Finish the job and reschedule it
            // I found it reruns fairly often
            this@MyJobService.jobFinished(params, true)
        }
    }
}