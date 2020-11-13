package com.example.playground.workmanager.work

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.playground.R
import com.example.playground.workmanager.util.blurBitmap
import com.example.playground.workmanager.util.makeStatusNotification
import com.example.playground.workmanager.util.writeBitmapToFile
import timber.log.Timber

class BlurWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {
        makeStatusNotification("Blurring image", applicationContext)
        return try {
            val picture = BitmapFactory.decodeResource(
                applicationContext.resources,
                R.drawable.test
            )

            val output = blurBitmap(picture, applicationContext)

            // Write bitmap to a temp file
            val outputUri = writeBitmapToFile(applicationContext, output)

            makeStatusNotification("Output is $outputUri", applicationContext)

            Result.success()
        } catch (throwable: Throwable) {
            Timber.e(throwable, "Error applying blur")
            Result.failure()
        }
    }
}