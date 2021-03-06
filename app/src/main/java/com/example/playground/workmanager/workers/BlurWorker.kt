package com.example.playground.workmanager.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.playground.workmanager.util.*
import timber.log.Timber
import java.lang.IllegalArgumentException
import kotlin.math.round

class BlurWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {
        // Makes a notification when the work starts and slows down the work so that
        // it's easier to see each WorkRequest start, even on emulated devices
        makeStatusNotification("Blurring image", applicationContext)
//        sleep()
        val randomInt = (0..90).random()
        setProgressAsync(workDataOf(PROGRESS to randomInt))
        sleep()

        val resourceUri = inputData.getString(KEY_IMAGE_URI)

        return try {
            if (TextUtils.isEmpty(resourceUri)) {
                Timber.e("Invalid input uri")
                throw IllegalArgumentException("Invalid input uri")
            }

            val resolver = applicationContext.contentResolver

            val picture = BitmapFactory.decodeStream(
                resolver.openInputStream(Uri.parse(resourceUri))
            )

            val output = blurBitmap(picture, applicationContext)

            // Write bitmap to a temp file
            val outputUri = writeBitmapToFile(applicationContext, output)

            val outputData = workDataOf(KEY_IMAGE_URI to "$outputUri")

            // Emulate smaller increments on delay (reduced sleep() value form 3 seconds to 0.3 seconds
            (randomInt..100 step 10).forEach {
                setProgressAsync(workDataOf(PROGRESS to it))
                sleep()
            }

            Result.success(outputData)
        } catch (throwable: Throwable) {
            Timber.e(throwable, "Error applying blur")
            Result.failure()
        }
    }
}