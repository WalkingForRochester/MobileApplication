package com.walkingforrochester.walkingforrochester.android.network.request

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AccountIdRequest(
    val accountId: Long
)