package com.walkingforrochester.walkingforrochester.android.repository

import android.net.Uri
import com.walkingforrochester.walkingforrochester.android.model.AccountProfile
import java.time.LocalDate


interface NetworkRepository {

    suspend fun fetchProfile(accountId: Long): AccountProfile

    suspend fun fetchProfile(email: String): AccountProfile

    suspend fun isEmailInUse(email: String): Boolean

    suspend fun updateProfile(profile: AccountProfile)

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

    suspend fun deleteUser(accountId: Long)
}