package com.walkingforrochester.walkingforrochester.android.network.response

import com.squareup.moshi.JsonClass
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
) : ErrorMessage