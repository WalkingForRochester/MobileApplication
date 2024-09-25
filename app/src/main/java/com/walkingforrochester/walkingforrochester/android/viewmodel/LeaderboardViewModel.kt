package com.walkingforrochester.walkingforrochester.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walkingforrochester.walkingforrochester.android.network.RestApiService
import com.walkingforrochester.walkingforrochester.android.network.request.LeaderboardRequest
import com.walkingforrochester.walkingforrochester.android.ui.state.LeaderData
import com.walkingforrochester.walkingforrochester.android.ui.state.LeaderboardFiltersState
import com.walkingforrochester.walkingforrochester.android.ui.state.LeaderboardScreenEvent
import com.walkingforrochester.walkingforrochester.android.ui.state.PeriodFilter
import com.walkingforrochester.walkingforrochester.android.ui.state.TypeFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val restApiService: RestApiService,
) : ViewModel() {

    private val _leaderboardFilters = MutableStateFlow(LeaderboardFiltersState())
    val leaderboardFilters = _leaderboardFilters.asStateFlow()

    private val _eventFlow = MutableSharedFlow<LeaderboardScreenEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val currentLeaders: StateFlow<LeaderData> = _leaderboardFilters.transform { filter ->
        emit(LeaderData(loading = true))
        emit(fetchLeaders(filter))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LeaderData()
    )

    private suspend fun fetchLeaders(filterState: LeaderboardFiltersState): LeaderData {
        return try {
            val type = filterState.type
            val result = restApiService.leaderboard(
                LeaderboardRequest(
                    orderBy = type.name.lowercase(),
                    startDate = filterState.period.startDate(),
                    endDate = filterState.period.endDate()
                )
            )
            LeaderData(
                leaders = result.filter { leader ->
                    TypeFilter.Collection == type && (leader.collection ?: 0) > 0
                        || TypeFilter.Distance == type && (leader.distance ?: 0.0) > 0.0
                        || TypeFilter.Duration == type && (leader.duration ?: 0) > 0
                }
            )
        } catch (t: Throwable) {
            Timber.e(t, "Couldn't load leaderboard from server")
            _eventFlow.emit(LeaderboardScreenEvent.UnexpectedError)
            LeaderData()
        }
    }

    fun onTypeFilterChange(type: TypeFilter) = _leaderboardFilters.update {
        it.copy(type = type)
    }

    fun onPeriodFilterChange(period: PeriodFilter) = _leaderboardFilters.update {
        it.copy(period = period)
    }

}