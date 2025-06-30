package com.walkingforrochester.walkingforrochester.android.repository.internal

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.walkingforrochester.walkingforrochester.android.di.AppModule.Companion.PREFERENCE_FILE
import com.walkingforrochester.walkingforrochester.android.model.AccountProfile
import com.walkingforrochester.walkingforrochester.android.model.PermissionPreferences
import com.walkingforrochester.walkingforrochester.android.model.UserPreferences
import com.walkingforrochester.walkingforrochester.android.repository.PreferenceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : PreferenceRepository {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        PREFERENCE_FILE,
        produceMigrations = {
            listOf(
                SharedPreferencesMigration(
                    context = it,
                    sharedPreferencesName = PREFERENCE_FILE
                )
            )
        }
    )

    private suspend fun cleanOldPreferences() {
        context.dataStore.edit { prefs ->
            prefs.remove(booleanPreferencesKey("walking_for_rochester_dont_ask_location_permissions"))
            prefs.remove(booleanPreferencesKey("walking_for_rochester_dont_ask_camera_permissions"))
        }
    }

    override val userPreferences: Flow<UserPreferences> = context.dataStore.data
        .onStart {
            cleanOldPreferences()
        }
        .map { data ->
            UserPreferences(
                isDarkMode = data[DARK_MODE_KEY] == true,
                accountId = data[ACCOUNT_ID_KEY] ?: AccountProfile.NO_ACCOUNT
            )
        }

    override val accountId: Flow<Long> = context.dataStore.data.map { data ->
        data[ACCOUNT_ID_KEY] ?: AccountProfile.NO_ACCOUNT
    }

    override suspend fun fetchAccountId(): Long {
        return accountId.first()
    }

    override suspend fun updateAccountId(accountId: Long) {
        context.dataStore.edit { data ->
            data[ACCOUNT_ID_KEY] = accountId
        }
    }

    override suspend fun updateDarkMode(enabled: Boolean) {
        context.dataStore.edit { data ->
            data[DARK_MODE_KEY] = enabled
        }
    }

    override suspend fun removeAccountInfo() {
        context.dataStore.edit { data ->
            data.remove(ACCOUNT_ID_KEY)
            data.remove(DARK_MODE_KEY)
        }
    }

    override val permissionPreferences: Flow<PermissionPreferences> =
        context.dataStore.data.map { data ->
            PermissionPreferences(
                locationRationalShown = data[LOCATION_RATIONAL_KEY] == true,
                notificationRationalShown = data[NOTIFICATION_RATIONAL_KEY] == true,
                cameraRationalShown = data[CAMERA_RATIONAL_KEY] == true
            )
        }


    override suspend fun updateLocationRationalShown(shown: Boolean) {
        context.dataStore.edit { data ->
            data[LOCATION_RATIONAL_KEY] = shown
        }
    }

    override suspend fun updateNotificationRationalShown(shown: Boolean) {
        context.dataStore.edit { data ->
            data[NOTIFICATION_RATIONAL_KEY] = shown
        }
    }

    override suspend fun updateCameraRationalShown(shown: Boolean) {
        context.dataStore.edit { data ->
            data[CAMERA_RATIONAL_KEY] = shown
        }
    }

    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("walking_for_rochester_dark_mode_enabled")
        private val ACCOUNT_ID_KEY = longPreferencesKey("walking_for_rochester_account_id")
        private val LOCATION_RATIONAL_KEY = booleanPreferencesKey(
            "walking_for_rochester_asked_location_permission_once"
        )
        private val NOTIFICATION_RATIONAL_KEY =
            booleanPreferencesKey("asked_notification_permission_once")
        private val CAMERA_RATIONAL_KEY =
            booleanPreferencesKey("walking_for_rochester_asked_camera_permission_once")
    }
}