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
    private var flag: Boolean = false
    /**
     * @return true means restart if killed
     * @return false means do not restart if killed
     */
    override fun onStopJob(params: JobParameters?): Boolean {
        Timber.d("onStopJob called")
        flag = false
        return true
    }

    /**
     * @return true means long duration task
     * @return false means short duration task
     */
    override fun onStartJob(params: JobParameters?): Boolean {
        Timber.d("onStartJob called and it should restart the service")
        flag = true
        doBackgroundWork()

        val intent = Intent(getString(R.string.intent_snack_key))
        intent.putExtra(getString(R.string.snack_content_key), "Job service has started running")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        return true
    }

    private fun doBackgroundWork() {
        var counter = 0
        var randomInt = 0
        jobScope.launch {
            while (flag) {
                delay(1000)
                if(flag) {
                    counter++
                    randomInt = (0..999).random()
                    Timber.d("Random value: $randomInt")
                    // Display a toast every 10 seconds
                    if (counter % 10 == 0) {
                        // Toast can not be displayed when called outside main thread
                       launch(Dispatchers.Main) {
                           toast("Random value: $randomInt", baseContext)
                       }
                    }
                }
            }
        }
    }
}