package com.walkingforrochester.walkingforrochester.android.network.request

import com.squareup.moshi.JsonClass
import java.time.LocalDate

@JsonClass(generateAdapter = true)
data class LogAWalkRequest(
    val accountId: Long,
    val distance: Double,
    val duration: Double,
    val collect: Int,
    val pickDate: LocalDate = LocalDate.now(),
    val imageFileName: String,
    val path: String
)