package com.walkingforrochester.walkingforrochester.android.repository

import com.walkingforrochester.walkingforrochester.android.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {

    val userPreferences: Flow<UserPreferences>
    val accountId: Flow<Long>

    suspend fun cleanOldPreferences()

    suspend fun fetchAccountId(): Long

    suspend fun updateAccountId(accountId: Long)

    val isDarkModeEnabled: Flow<Boolean>

    suspend fun updateDarkMode(enabled: Boolean)

    suspend fun removeAccountInfo()

    suspend fun locationRationalShown(): Boolean

    suspend fun updateLocationRationalShown(shown: Boolean)

    suspend fun notificationRationalShown(): Boolean

    suspend fun updateNotificationRationalShown(shown: Boolean)

    suspend fun cameraRationalShown(): Boolean

    suspend fun updateCameraRationalShown(shown: Boolean)
}