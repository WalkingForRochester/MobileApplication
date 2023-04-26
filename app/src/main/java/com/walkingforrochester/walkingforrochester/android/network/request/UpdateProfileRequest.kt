package com.walkingforrochester.walkingforrochester.android.network.request

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateProfileRequest (
    val accountId: Long,
    val email: String,
    val phone: String,
    val nickname: String,
    val communityService: Boolean,
    val imgUrl: String
)