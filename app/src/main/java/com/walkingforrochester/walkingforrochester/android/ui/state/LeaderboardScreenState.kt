package com.walkingforrochester.walkingforrochester.android.ui.state

import com.walkingforrochester.walkingforrochester.android.model.Leader
import java.time.LocalDate

data class LeaderData(
    val leaders: List<Leader> = emptyList(),
    val loading: Boolean = false
)

data class LeaderboardFiltersState(
    val type: TypeFilter = TypeFilter.Collection,
    val period: PeriodFilter = PeriodFilter.Week
)

enum class TypeFilter {
    Collection,
    Distance,
    Duration
}

enum class PeriodFilter(val startDate: () -> LocalDate, val endDate: () -> LocalDate) {
    Day(startDate = { LocalDate.now() }, endDate = { LocalDate.now() }),
    Week(startDate = { LocalDate.now().minusWeeks(1) }, endDate = { LocalDate.now() }),
    Month(startDate = { LocalDate.now().minusMonths(1) }, endDate = { LocalDate.now() }),
    Year(startDate = { LocalDate.now().minusYears(1) }, endDate = { LocalDate.now() });
}

enum class LeaderboardScreenEvent {
    UnexpectedError
}