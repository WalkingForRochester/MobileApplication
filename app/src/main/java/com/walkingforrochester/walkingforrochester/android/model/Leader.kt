package com.walkingforrochester.walkingforrochester.android.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Leader(
    val place: Long,
    val accountId: Long,
    val firstName: String?,
    val nickname: String?,
    val imgUrl: String?,
    val collection: Long?,
    val distance: Double?,
    val duration: Long?
)