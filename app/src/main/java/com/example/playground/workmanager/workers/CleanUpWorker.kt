package com.example.playground.workmanager.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.playground.workmanager.util.OUTPUT_PATH
import com.example.playground.workmanager.util.makeStatusNotification
import com.example.playground.workmanager.util.sleep
import timber.log.Timber
import java.io.File
import java.lang.Exception

/**
 * Cleans up temporary files generated during blurring process
 */
class CleanUpWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {
        // Makes a notification when the work starts and slows down the work so that
        // it's easier to see each WorkRequest start, even on emulated devices
        makeStatusNotification("Cleaning up old temporary files", applicationContext)
        sleep()

        return try {
            val outputDirectory = File(applicationContext.filesDir, OUTPUT_PATH)
            if(outputDirectory.exists()) {
                val entries = outputDirectory.listFiles()
                entries?.let {
                    it.forEach { file ->
                        val name = file.name
                        if(name.isNotEmpty() && name.endsWith(".png")) {
                            val deleted = file.delete()
                            Timber.i("Deleted $name - $deleted")
                        }
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Timber.e(e)
            Result.failure()
        }
    }
}