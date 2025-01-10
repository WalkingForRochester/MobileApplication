package com.walkingforrochester.walkingforrochester.android.repository

interface PreferenceRepository {

    suspend fun fetchAccountId(): Long

    suspend fun updateAccountId(accountId: Long)

    suspend fun isDarkModeEnabled(): Boolean

    suspend fun updateDarkMode(enabled: Boolean)

    suspend fun removeAccountInfo()
}