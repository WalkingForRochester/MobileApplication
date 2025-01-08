package com.walkingforrochester.walkingforrochester.android.repository

interface PreferenceRepository {

    suspend fun fetchAccountId(): Long

    suspend fun removeAccountInfo(): Boolean
}