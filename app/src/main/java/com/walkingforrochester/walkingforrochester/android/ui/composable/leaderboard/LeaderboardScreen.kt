package com.walkingforrochester.walkingforrochester.android.ui.composable.leaderboard

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.model.Leader
import com.walkingforrochester.walkingforrochester.android.model.LeaderboardPeriod
import com.walkingforrochester.walkingforrochester.android.model.LeaderboardType
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.LocalSnackbarHostState
import com.walkingforrochester.walkingforrochester.android.ui.state.LeaderData
import com.walkingforrochester.walkingforrochester.android.ui.state.LeaderboardFiltersState
import com.walkingforrochester.walkingforrochester.android.ui.state.LeaderboardScreenEvent
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme
import com.walkingforrochester.walkingforrochester.android.viewmodel.LeaderboardViewModel

@Composable
fun LeaderboardScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    leaderboardViewModel: LeaderboardViewModel = hiltViewModel()
) {
    val filtersState by leaderboardViewModel.leaderboardFilters.collectAsStateWithLifecycle()
    val leaderData by leaderboardViewModel.currentLeaders.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = LocalSnackbarHostState.current

    LaunchedEffect(lifecycleOwner, leaderboardViewModel) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            leaderboardViewModel.eventFlow.collect { event ->
                when (event) {
                    LeaderboardScreenEvent.UnexpectedError -> {
                        snackbarHostState.showSnackbar(context.getString(R.string.unexpected_error))
                    }
                }
            }
        }
    }

    LeaderboardContent(
        filtersState = filtersState,
        leaderData = leaderData,
        modifier = modifier,
        contentPadding = contentPadding,
        onTypeFilterChange = { type -> leaderboardViewModel.onTypeFilterChange(type) },
        onPeriodFilterChange = { period -> leaderboardViewModel.onPeriodFilterChange(period) }
    )
}

@Composable
private fun LeaderboardContent(
    filtersState: LeaderboardFiltersState,
    leaderData: LeaderData,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    onTypeFilterChange: (LeaderboardType) -> Unit = {},
    onPeriodFilterChange: (LeaderboardPeriod) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            LeaderFilters(
                modifier = Modifier.padding(bottom = 8.dp),
                currentType = filtersState.type,
                currentPeriod = filtersState.period,
                onTypeFilterChange = onTypeFilterChange,
                onPeriodFilterChange = onPeriodFilterChange
            )
        }
        when {
            leaderData.loading -> {
                item {
                    Box(modifier = Modifier.fillParentMaxHeight(.5f)) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            leaderData.leaders.isNotEmpty() -> {
                itemsIndexed(
                    items = leaderData.leaders,
                    key = { _, item -> item.accountId }
                ) { index, leader ->
                    LeaderCard(
                        leader = leader,
                        index = index,
                        type = filtersState.type
                    )
                }
            }

            else -> {
                item {
                    Box {
                        Text(
                            text = stringResource(R.string.empty_leaderboard),
                            modifier = Modifier
                                .fillMaxWidth(1f)
                                .padding(horizontal = 16.dp, vertical = 24.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewLeaderboardScreen() {
    WalkingForRochesterTheme {
        Surface {
            LeaderboardContent(
                filtersState = LeaderboardFiltersState(),
                leaderData = LeaderData(
                    loading = false,
                    leaders = listOf(
                        Leader(
                            collectionPosition = 3,
                            accountId = 2,
                            firstName = "John",
                            nickname = "",
                            imgUrl = "",
                            collection = 13L,
                            distance = 3.3,
                            duration = 7200L
                        )
                    )
                )
            )
        }
    }
}
