package com.walkingforrochester.walkingforrochester.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walkingforrochester.walkingforrochester.android.di.DefaultDispatcher
import com.walkingforrochester.walkingforrochester.android.model.Leader
import com.walkingforrochester.walkingforrochester.android.model.LeaderboardPeriod
import com.walkingforrochester.walkingforrochester.android.model.LeaderboardType
import com.walkingforrochester.walkingforrochester.android.repository.NetworkRepository
import com.walkingforrochester.walkingforrochester.android.ui.state.LeaderData
import com.walkingforrochester.walkingforrochester.android.ui.state.LeaderboardFiltersState
import com.walkingforrochester.walkingforrochester.android.ui.state.LeaderboardScreenEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val networkRepository: NetworkRepository,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {
    val leaderboardFilters = _leaderboardFilters.asStateFlow()

    private val _eventFlow = MutableSharedFlow<LeaderboardScreenEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var lastLeaderList: List<Leader> = emptyList()
    private var lastFetchTime = 0L
    private var lastFetchPeriod = _leaderboardFilters.value.period

    val currentLeaders: StateFlow<LeaderData> = _leaderboardFilters.transform { filter ->
        if (lastFetchPeriod != filter.period ||
            lastFetchTime + REFRESH_INTERVAL < System.currentTimeMillis()
        ) {
            emit(LeaderData(loading = true))
        }
        emit(determineLeaders(filter))
    }.catch { throwable ->
        Timber.e(throwable, "Unexpected error fetching leaders")
        _eventFlow.emit(LeaderboardScreenEvent.UnexpectedError)
        emit(LeaderData())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LeaderData(loading = true)
    )

    private suspend fun determineLeaders(
        filterState: LeaderboardFiltersState
    ): LeaderData = withContext(defaultDispatcher) {

        if (lastFetchPeriod != filterState.period ||
            lastFetchTime + REFRESH_INTERVAL < System.currentTimeMillis()
        ) {
            val time = System.currentTimeMillis()
            lastLeaderList = networkRepository.fetchLeaderboard(filterState.period)
            lastFetchPeriod = filterState.period
            lastFetchTime = System.currentTimeMillis()

            // Want a minimum delay of 300ms just to not cause UI to jump between
            // loading complete state so fast it looks like a flicker.
            delay(max(0L, 300L - (lastFetchTime - time)))
        }

        val comparator = when (filterState.type) {
            LeaderboardType.Collection -> Leader.collectionComparator
            LeaderboardType.Distance -> Leader.distanceComparator
            LeaderboardType.Duration -> Leader.durationComparator
        }

        val leaders = lastLeaderList.asSequence()
            .filter {
                when (filterState.type) {
                    LeaderboardType.Collection -> it.collection > 0
                    LeaderboardType.Distance -> it.distance > 0.0
                    LeaderboardType.Duration -> it.duration > 0
                }
            }
            .sortedWith(comparator)
            .toList()

        LeaderData(leaders = leaders)
    }

    fun onTypeFilterChange(type: LeaderboardType) = _leaderboardFilters.update {
        it.copy(type = type)
    }

    fun onPeriodFilterChange(period: LeaderboardPeriod) = _leaderboardFilters.update {
        it.copy(period = period)
    }

    companion object {
        const val REFRESH_INTERVAL = 10 * 60L * 1000L // ten minutes
        private val _leaderboardFilters = MutableStateFlow(LeaderboardFiltersState())
    }
}