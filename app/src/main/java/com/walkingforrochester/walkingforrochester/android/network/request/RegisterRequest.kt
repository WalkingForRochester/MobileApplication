package com.walkingforrochester.walkingforrochester.android.network.request

import com.squareup.moshi.JsonClass
import java.time.LocalDate

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val dateOfBirth: LocalDate = LocalDate.now(),
    val nickname: String,
    val communityService: Boolean,
    val facebookId: String?
)