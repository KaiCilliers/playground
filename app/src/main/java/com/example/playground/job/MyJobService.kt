package com.example.playground.job

import android.app.job.JobParameters
import android.app.job.JobService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

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
        return true
    }

    private fun doBackgroundWork() {
        jobScope.launch {
            while (flag) {
                delay(1000)
                if(flag) {
                    Timber.d("Random value: ${(0..100).random()}")
                }
            }
        }
    }
}