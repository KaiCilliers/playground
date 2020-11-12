package com.example.playground.workmanager

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel

class BlurViewModel (application: Application) : AndroidViewModel(application) {
    internal var imageUri: Uri? = null
    internal var outputUri: Uri? = null

    /**
     * Create the WorkRequest to apply the blur
     * and save the resulting image
     * @param blurLevel The amount to blur the image
     */
    internal fun applyBlur(blurLevel: Int) {}

    private fun uriOrNull(uriString: String?): Uri? {
        return if (!uriString.isNullOrEmpty()) {
            Uri.parse(uriString)
        } else {
            null
        }
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