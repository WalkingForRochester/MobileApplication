package com.walkingforrochester.walkingforrochester.android.repository

import android.net.Uri
import com.walkingforrochester.walkingforrochester.android.model.AccountProfile
import com.walkingforrochester.walkingforrochester.android.model.Leader
import com.walkingforrochester.walkingforrochester.android.model.LeaderboardPeriod
import java.time.LocalDate


interface NetworkRepository {

    suspend fun fetchProfile(accountId: Long): AccountProfile

    suspend fun fetchAccountId(email: String): Long

    suspend fun isEmailInUse(email: String): Boolean

    suspend fun updateProfile(profile: AccountProfile)

    suspend fun performLogin(email: String, password: String): Long

    suspend fun registerAccount(profile: AccountProfile, password: String): Long

    suspend fun forgotPassword(email: String): String

    suspend fun resetPassword(email: String, password: String)

    suspend fun fetchLeaderboard(
        period: LeaderboardPeriod
    ) : List<Leader>

    suspend fun uploadProfileImage(
        accountId: Long,
        imageUri: Uri,
        time: LocalDate = LocalDate.now()
    ): String

    suspend fun uploadWalkImage(
        accountId: Long,
        imageUri: Uri,
        time: LocalDate = LocalDate.now()
    ): String

    suspend fun submitWalk(
        accountId: Long,
        bagsCollected: Int,
        distanceInMiles: Double,
        duration: Long,
        imageFileName: String,
        encodedPolyline: String
    )

    suspend fun deleteUser(accountId: Long)
}