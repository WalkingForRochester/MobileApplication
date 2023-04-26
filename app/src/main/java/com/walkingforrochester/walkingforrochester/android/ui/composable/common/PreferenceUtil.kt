package com.walkingforrochester.walkingforrochester.android.ui.composable.common

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.getPreference
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

@Composable
fun preferenceState(key: String, defaultValue: Any): State<Any?> {
    val context = LocalContext.current

    return produceState(initialValue = getPreference(context, key, defaultValue)) {
        context.observePreferenceAsFlow(key, defaultValue).collect { value = it }
    }
}

@Composable
fun booleanPreferenceState(key: String, defaultValue: Boolean): State<Boolean> {
    return preferenceState(key = key, defaultValue = defaultValue) as State<Boolean>
}

@Composable
fun longPreferenceState(key: String, defaultValue: Long): State<Long> {
    return preferenceState(key = key, defaultValue = defaultValue) as State<Long>
}

fun Context.observePreferenceAsFlow(key: String, defaultValue: Any) = callbackFlow {
    val sharedPrefs = getSharedPreferences(
        getString(R.string.wfr_preferences),
        Context.MODE_PRIVATE
    )

    val listener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (changedKey?.equals(key) == true) {
                trySend(getPreference(this@observePreferenceAsFlow, key, defaultValue))
            }
        }

    sharedPrefs.registerOnSharedPreferenceChangeListener(listener)

    // Remove callback when not used
    awaitClose {
        // Remove listeners
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener)
    }
}