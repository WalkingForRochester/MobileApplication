package com.walkingforrochester.walkingforrochester.android.ui.composable.leaderboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme
import com.walkingforrochester.walkingforrochester.android.viewmodel.LeaderboardViewModel

@Composable
fun LeaderboardScreen(
    modifier: Modifier = Modifier,
    leaderboardViewModel: LeaderboardViewModel = hiltViewModel()
) {
    val uiState by leaderboardViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.filtersState) {
        leaderboardViewModel.fetchLeaders()
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LeaderFilters(
            modifier.padding(top = 4.dp),
            onTypeFilterChange = leaderboardViewModel::onTypeFilterChange,
            onPeriodFilterChange = leaderboardViewModel::onPeriodFilterChange
        )
        when {
            uiState.loading -> {
                Box(modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Black
                    )
                }
            }

            uiState.leaders.isNotEmpty() -> {
                LeaderList(leaders = uiState.leaders, type = uiState.filtersState.type)
            }

            else -> {
                Box(modifier.fillMaxSize()) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = stringResource(R.string.empty_leaderboard),
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLeaderboardScreen() {
    WalkingForRochesterTheme {
        LeaderboardScreen()
    }
}
