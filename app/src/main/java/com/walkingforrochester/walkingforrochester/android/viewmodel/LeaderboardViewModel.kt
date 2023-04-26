package com.walkingforrochester.walkingforrochester.android.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.walkingforrochester.walkingforrochester.android.network.RestApiService
import com.walkingforrochester.walkingforrochester.android.network.request.LeaderboardRequest
import com.walkingforrochester.walkingforrochester.android.showUnexpectedErrorToast
import com.walkingforrochester.walkingforrochester.android.ui.state.LeaderboardScreenState
import com.walkingforrochester.walkingforrochester.android.ui.state.PeriodFilter
import com.walkingforrochester.walkingforrochester.android.ui.state.TypeFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val restApiService: RestApiService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardScreenState())
    val uiState = _uiState.asStateFlow()

    fun fetchLeaders() = flow<Nothing> {
        _uiState.update { it.copy(loading = true) }
        with(_uiState.value.filtersState) {
            val result = restApiService.leaderboard(
                LeaderboardRequest(
                    orderBy = type.orderBy,
                    startDate = period.startDate(),
                    endDate = period.endDate()
                )
            )

            _uiState.update {
                it.copy(leaders = result.filter { leader ->
                    TypeFilter.Collection == type && (leader.collection ?: 0) > 0
                            || TypeFilter.Distance == type && (leader.distance ?: 0.0) > 0.0
                            || TypeFilter.Duration == type && (leader.duration ?: 0) > 0
                })
            }
        }
    }.catch {
        Timber.e(it, "Couldn't load leaderboard from server")
        showUnexpectedErrorToast(context)
    }.onCompletion {
        _uiState.update { it.copy(loading = false) }
    }.launchIn(viewModelScope)

    fun onTypeFilterChange(type: TypeFilter) = _uiState.update {
        it.copy(
            filtersState = it.filtersState.copy(type = type)
        )
    }

    fun onPeriodFilterChange(period: PeriodFilter) = _uiState.update {
        it.copy(
            filtersState = it.filtersState.copy(period = period)
        )
    }

}