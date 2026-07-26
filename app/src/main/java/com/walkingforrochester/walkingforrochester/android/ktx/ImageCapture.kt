package com.walkingforrochester.walkingforrochester.android.ktx

import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.File
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun ImageCapture.takePicture(captureFile: File, executor: Executor): Uri? {

    return suspendCancellableCoroutine { continuation ->
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(captureFile).build()
        val imageSavedCallback = object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                continuation.resume(output.savedUri)
            }

            override fun onError(ex: ImageCaptureException) {
                Timber.e(ex, "Image capture failed")
                continuation.resumeWithException(ex)
            }
        }

        takePicture(
            outputFileOptions,
            executor,
            imageSavedCallback
        )

        continuation.invokeOnCancellation { Timber.w("Image capture cancelled") }
    }
}