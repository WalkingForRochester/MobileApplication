package com.walkingforrochester.walkingforrochester.android.model

data class AccountProfile(
    val accountId: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val imageUrl: String,
    val nickname: String,
    val communityService: Boolean,
    val distanceToday: Double,
    val totalDistance: Double,
    val durationToday: Long,
    val totalDuration: Long,
    val facebookId: String?,
) {
    companion object {
        const val NO_ACCOUNT = -1L

        val DEFAULT_PROFILE = AccountProfile(
            accountId = NO_ACCOUNT,
            email = "",
            firstName = "",
            lastName = "",
            phoneNumber = "",
            imageUrl = "",
            nickname = "",
            communityService = false,
            distanceToday = 0.0,
            totalDistance = 0.0,
            durationToday = 0,
            totalDuration = 0,
            facebookId = null
        )
    }
}