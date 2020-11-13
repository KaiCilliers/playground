package com.example.playground.workmanager

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.work.*
import com.example.playground.workmanager.util.IMAGE_MANIPULATION_WORK_NAME
import com.example.playground.workmanager.util.KEY_IMAGE_URI
import com.example.playground.workmanager.util.TAG_OUTPUT
import com.example.playground.workmanager.util.TAG_PROGRESS
import com.example.playground.workmanager.workers.BlurWorker
import com.example.playground.workmanager.workers.CleanUpWorker
import com.example.playground.workmanager.workers.SaveImageToFileWorker

class BlurViewModel (application: Application) : AndroidViewModel(application) {

    private val workManager by lazy { WorkManager.getInstance(application) }

    internal val outputWorkInfos: LiveData<List<WorkInfo>>
    internal val progressWorkInfoItems: LiveData<List<WorkInfo>>

    internal var imageUri: Uri? = null
    internal var outputUri: Uri? = null

    init {
        // This transformation makes sure that whenever the current
        // work Id changes the WorkInfo the UI is listening to changes
        outputWorkInfos = workManager.getWorkInfosByTagLiveData(TAG_OUTPUT)
        progressWorkInfoItems = workManager.getWorkInfosByTagLiveData(TAG_PROGRESS)
    }

    /**
     * Create the WorkRequest to apply the blur
     * and save the resulting image
     * @param blurLevel The amount to blur the image
     */
    internal fun applyBlur(blurLevel: Int) {
        // Add WorkRequest to clean up temporary images
        // Unique work chain to only blur a single image
        // at a time
        var continuation = workManager
            .beginUniqueWork(
                IMAGE_MANIPULATION_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequest.from(CleanUpWorker::class.java)
            )

        // Add WorkRequest to blur the image the number of times requested
        // Calling blur code 3 times is less than efficient than having a
        // BlurWorker take in an input that controls the "level" of blur
        // But it showcases the flexibility of WorkManager chaining
        for (i in 0 until blurLevel) {
            val blurBuilder = OneTimeWorkRequestBuilder<BlurWorker>()
            // Input the Uri if this is the first blur operation
            // After the first blur operation the input will be the output of previous
            // blur operations.
            if (i == 0) {
                blurBuilder.setInputData(createInputDataForUri())
            }
            blurBuilder.addTag(TAG_PROGRESS)
            continuation = continuation.then(blurBuilder.build())
        }

        // Some Constraints
        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .setRequiresBatteryNotLow(true)
            .build()

        // Add WorkRequest to save the image to the filesystem
        val save = OneTimeWorkRequestBuilder<SaveImageToFileWorker>()
            .setConstraints(constraints)
            .addTag(TAG_OUTPUT)
            .build()

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

    internal fun cancelWork() {
        workManager.cancelUniqueWork(IMAGE_MANIPULATION_WORK_NAME)
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