package com.walkingforrochester.walkingforrochester.android.network.request

import com.squareup.moshi.JsonClass
import java.time.LocalDate

@JsonClass(generateAdapter = true)
data class LeaderboardRequest(
    val orderBy: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)