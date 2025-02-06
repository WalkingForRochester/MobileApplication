package com.walkingforrochester.walkingforrochester.android.repository.internal

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.di.IODispatcher
import com.walkingforrochester.walkingforrochester.android.model.AccountProfile
import com.walkingforrochester.walkingforrochester.android.repository.PreferenceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPreferences: SharedPreferences,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : PreferenceRepository {

    override suspend fun fetchAccountId(): Long = withContext(ioDispatcher) {
        sharedPreferences.getLong(
            context.getString(R.string.wfr_account_id),
            AccountProfile.NO_ACCOUNT
        )
    }

    override suspend fun updateAccountId(accountId: Long) = withContext(ioDispatcher) {
        sharedPreferences.edit(commit = true) {
            putLong(context.getString(R.string.wfr_account_id), accountId)
        }
    }

    override suspend fun isDarkModeEnabled(): Boolean = withContext(ioDispatcher) {
        sharedPreferences.getBoolean(
            context.getString(R.string.wfr_dark_mode_enabled),
            false
        )
    }

    override suspend fun updateDarkMode(enabled: Boolean) = withContext(ioDispatcher) {
        sharedPreferences.edit(commit = true) {
            putBoolean(context.getString(R.string.wfr_dark_mode_enabled), enabled)
        }
    }

    override suspend fun removeAccountInfo() = withContext(ioDispatcher) {
        sharedPreferences.edit(commit = true) {
            remove(context.getString(R.string.wfr_account_id))
            remove(context.getString(R.string.wfr_dark_mode_enabled))
        }
    }

    override suspend fun locationRationalShown(): Boolean = withContext(ioDispatcher) {
        sharedPreferences.getBoolean(LOCATION_RATIONAL_KEY, false)
    }

    override suspend fun updateLocationRationalShown(shown: Boolean) = withContext(ioDispatcher) {
        sharedPreferences.edit(commit = true) {
            putBoolean(LOCATION_RATIONAL_KEY, shown)
        }
    }

    override suspend fun cameraRationalShown(): Boolean = withContext(ioDispatcher) {
        sharedPreferences.getBoolean(CAMERA_RATIONAL_KEY, false)
    }

    override suspend fun updateCameraRationalShown(shown: Boolean) = withContext(ioDispatcher) {
        sharedPreferences.edit(commit = true) {
            putBoolean(CAMERA_RATIONAL_KEY, shown)
        }
    }

    companion object {
        private const val LOCATION_RATIONAL_KEY =
            "walking_for_rochester_asked_location_permission_once"
        private const val CAMERA_RATIONAL_KEY = "walking_for_rochester_asked_camera_permission_once"
    }
}