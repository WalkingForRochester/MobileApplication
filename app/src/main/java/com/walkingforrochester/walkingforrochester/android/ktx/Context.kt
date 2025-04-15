package com.walkingforrochester.walkingforrochester.android.ktx

import android.content.Context
import android.content.Intent
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import timber.log.Timber
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun Context.safeStartActivity(intent: Intent) {
    try {
        startActivity(intent)
    } catch (t: Throwable) {
        Timber.d("Unable to start activity: %s", t.message)
    }
}

val Context.executor: Executor get() = ContextCompat.getMainExecutor(this)

suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { future ->
        future.addListener({
            continuation.resume(future.get())
        }, executor)
    }
}
