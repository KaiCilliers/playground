package com.example.playground.workmanager

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.playground.workmanager.util.KEY_IMAGE_URI
import com.example.playground.workmanager.workers.BlurWorker
import com.example.playground.workmanager.workers.CleanUpWorker
import com.example.playground.workmanager.workers.SaveImageToFileWorker

class BlurViewModel (application: Application) : AndroidViewModel(application) {

    private val workManager by lazy { WorkManager.getInstance(application) }

    internal var imageUri: Uri? = null
    internal var outputUri: Uri? = null

    /**
     * Create the WorkRequest to apply the blur
     * and save the resulting image
     * @param blurLevel The amount to blur the image
     */
    internal fun applyBlur(blurLevel: Int) {
        // Add WorkRequest to clean up temporary images
        var continuation = workManager
            .beginWith(
                OneTimeWorkRequest.from(CleanUpWorker::class.java)
            )

        // Add WorkRequest to blur the image
        val blurRequest = OneTimeWorkRequest.Builder(BlurWorker::class.java)
            .setInputData(createInputDataForUri())
            .build()

        continuation = continuation.then(blurRequest)

        // Add WorkRequest to save the image to the filesystem
        val save = OneTimeWorkRequest.Builder(SaveImageToFileWorker::class.java).build()

        continuation = continuation.then(save)

        // Actually start the work
        continuation.enqueue()
    }

    private fun uriOrNull(uriString: String?): Uri? {
        return if (!uriString.isNullOrEmpty()) {
            Uri.parse(uriString)
        } else {
            null
        }
    }

    /**
     * Creates the input data bundle which includes the Uri
     * to operate on
     * @return Data which contains the Image Uri as a String
     */
    private fun createInputDataForUri(): Data {
        val data = Data.Builder()
        imageUri?.let {
            data.putString(KEY_IMAGE_URI, "$imageUri")
        }
        return data.build()
    }

    /**
     * Setters
     */
    internal fun setImageUri(uri: String?) {
        imageUri = uriOrNull(uri)
    }

    internal fun setOutputUri(outputImageUri: String?) {
        outputUri = uriOrNull(outputImageUri)
    }
}