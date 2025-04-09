package com.walkingforrochester.walkingforrochester.android.repository

import com.walkingforrochester.walkingforrochester.android.model.PermissionPreferences
import com.walkingforrochester.walkingforrochester.android.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferenceRepository {

    val userPreferences: Flow<UserPreferences>
    val accountId: Flow<Long>

    suspend fun fetchAccountId(): Long

    suspend fun updateAccountId(accountId: Long)

    suspend fun updateDarkMode(enabled: Boolean)

    suspend fun removeAccountInfo()

    val permissionPreferences: Flow<PermissionPreferences>

    suspend fun updateLocationRationalShown(shown: Boolean)

    suspend fun updateNotificationRationalShown(shown: Boolean)

    suspend fun updateCameraRationalShown(shown: Boolean)
}