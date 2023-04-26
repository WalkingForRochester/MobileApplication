package com.walkingforrochester.walkingforrochester.android.ui.state

import com.walkingforrochester.walkingforrochester.android.model.Leader
import java.time.LocalDate

data class LeaderboardScreenState(
    var filtersState: LeaderboardFiltersState = LeaderboardFiltersState(),
    var leaders: List<Leader> = listOf(),
    var loading: Boolean = false
)

data class LeaderboardFiltersState(
    val type: TypeFilter = TypeFilter.Collection,
    val period: PeriodFilter = PeriodFilter.Day
)

enum class TypeFilter(val orderBy: String) {
    Collection("collection"),
    Distance("distance"),
    Duration("duration");
}

enum class PeriodFilter(val startDate: () -> LocalDate, val endDate: () -> LocalDate) {
    Day(startDate = { LocalDate.now() }, endDate = { LocalDate.now() }),
    Week(startDate = { LocalDate.now().minusWeeks(1) }, endDate = { LocalDate.now() }),
    Month(startDate = { LocalDate.now().minusMonths(1) }, endDate = { LocalDate.now() }),
    Year(startDate = { LocalDate.now().minusYears(1) }, endDate = { LocalDate.now() });
}
