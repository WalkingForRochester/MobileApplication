package com.walkingforrochester.walkingforrochester.android.network.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val accountId: Long?,
    val error: String?
)