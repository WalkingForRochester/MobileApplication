package com.walkingforrochester.walkingforrochester.android.repository.internal

import android.content.SharedPreferences
import androidx.core.content.edit
import com.walkingforrochester.walkingforrochester.android.di.IODispatcher
import com.walkingforrochester.walkingforrochester.android.model.AccountProfile
import com.walkingforrochester.walkingforrochester.android.repository.PreferenceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : PreferenceRepository {

    private fun cleanOldPreferences() {
        sharedPreferences.edit(commit = true) {
            remove("walking_for_rochester_dont_ask_location_permissions")
            remove("walking_for_rochester_dont_ask_camera_permissions")
        }
    }

    override suspend fun fetchAccountId(): Long = withContext(ioDispatcher) {
        sharedPreferences.getLong(ACCOUNT_ID_KEY, AccountProfile.NO_ACCOUNT)
    }

    override suspend fun updateAccountId(accountId: Long) = withContext(ioDispatcher) {
        sharedPreferences.edit(commit = true) {
            putLong(ACCOUNT_ID_KEY, accountId)
        }
    }

    override suspend fun isDarkModeEnabled(): Boolean = withContext(ioDispatcher) {
        cleanOldPreferences()
        sharedPreferences.getBoolean(DARK_MODE_KEY, false)
    }

    override suspend fun updateDarkMode(enabled: Boolean) = withContext(ioDispatcher) {
        sharedPreferences.edit(commit = true) {
            putBoolean(DARK_MODE_KEY, enabled)
        }
    }

    override suspend fun removeAccountInfo() = withContext(ioDispatcher) {
        sharedPreferences.edit(commit = true) {
            remove(ACCOUNT_ID_KEY)
            remove(DARK_MODE_KEY)
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
        private const val DARK_MODE_KEY = "walking_for_rochester_dark_mode_enabled"
        private const val ACCOUNT_ID_KEY = "walking_for_rochester_account_id"
        private const val LOCATION_RATIONAL_KEY =
            "walking_for_rochester_asked_location_permission_once"
        private const val CAMERA_RATIONAL_KEY = "walking_for_rochester_asked_camera_permission_once"
    }
}