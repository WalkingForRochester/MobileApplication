package com.walkingforrochester.walkingforrochester.android.ktx

import android.content.Context
import android.content.Intent
import timber.log.Timber

fun Context.safeStartActivity(intent: Intent) {
    try {
        startActivity(intent)
    } catch (t: Throwable) {
        Timber.d("Unable to start activity: %s", t.message)
    }
}