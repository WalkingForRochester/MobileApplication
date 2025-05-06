package com.walkingforrochester.walkingforrochester.android.network.response

import com.squareup.moshi.JsonClass
import com.walkingforrochester.walkingforrochester.android.model.AccountProfile
import java.time.LocalDate

@JsonClass(generateAdapter = true)
data class AccountResponse(
    val accountId: Long?,
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val dateOfBirth: LocalDate?,
    val phoneNumber: String?,
    val active: Short?,
    val permission: Short?,
    val imgUrl: String?,
    val nickname: String?,
    val communityService: Boolean?,
    val distance: Double?,
    val totalDistance: Double?,
    val duration: Long?,
    val totalDuration: Long?,
    val facebookId: String?,
    override val error: String?
) : ErrorMessage {

    fun toAccountProfile(): AccountProfile {
        return AccountProfile(
            accountId = accountId ?: AccountProfile.NO_ACCOUNT,
            email = email ?: "",
            firstName = firstName ?: "",
            lastName = lastName ?: "",
            phoneNumber = phoneNumber ?: "",
            imageUrl = imgUrl ?: "",
            nickname = nickname ?: "",
            communityService = communityService == true,
            distanceToday = distance ?: 0.0,
            totalDistance = totalDistance ?: 0.0,
            durationToday = duration ?: 0L,
            totalDuration = totalDuration ?: 0L,
            facebookId = facebookId
        )
    }
}