package com.walkingforrochester.walkingforrochester.android.ktx

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import timber.log.Timber
import java.util.concurrent.Executor

fun Context.safeStartActivity(intent: Intent) {
    try {
        startActivity(intent)
    } catch (t: Throwable) {
        Timber.d("Unable to start activity: %s", t.message)
    }
}

val Context.mainExecutorCompat: Executor get() = ContextCompat.getMainExecutor(this)
