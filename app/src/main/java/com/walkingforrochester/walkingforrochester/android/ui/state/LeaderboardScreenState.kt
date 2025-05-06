package com.walkingforrochester.walkingforrochester.android.ui.state

import com.walkingforrochester.walkingforrochester.android.model.Leader
import com.walkingforrochester.walkingforrochester.android.model.LeaderboardPeriod
import com.walkingforrochester.walkingforrochester.android.model.LeaderboardType

data class LeaderData(
    val leaders: List<Leader> = emptyList(),
    val loading: Boolean = false
)

data class LeaderboardFiltersState(
    val type: LeaderboardType = LeaderboardType.Collection,
    val period: LeaderboardPeriod = LeaderboardPeriod.Week
)

enum class LeaderboardScreenEvent {
    UnexpectedError
}