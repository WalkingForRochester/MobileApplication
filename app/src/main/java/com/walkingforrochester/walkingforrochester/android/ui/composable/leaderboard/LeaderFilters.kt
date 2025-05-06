package com.walkingforrochester.walkingforrochester.android.ui.composable.leaderboard

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.walkingforrochester.walkingforrochester.android.model.LeaderboardPeriod
import com.walkingforrochester.walkingforrochester.android.model.LeaderboardType
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@Composable
fun LeaderFilters(
    modifier: Modifier = Modifier,
    currentType: LeaderboardType,
    currentPeriod: LeaderboardPeriod,
    onTypeFilterChange: (LeaderboardType) -> Unit = {},
    onPeriodFilterChange: (LeaderboardPeriod) -> Unit = {}
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val colors = SegmentedButtonDefaults.colors().copy(
            activeContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            activeBorderColor = MaterialTheme.colorScheme.surfaceVariant,
            activeContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            inactiveContainerColor = MaterialTheme.colorScheme.surface,
            inactiveBorderColor = MaterialTheme.colorScheme.surfaceVariant,
            inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledActiveContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            disabledActiveBorderColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledInactiveBorderColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledActiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SingleChoiceSegmentedButtonRow {
            LeaderboardType.entries.forEach { entry ->
                SegmentedButton(
                    selected = currentType == entry,
                    onClick = { onTypeFilterChange(entry) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = entry.ordinal,
                        count = LeaderboardType.entries.size
                    ),
                    colors = colors
                ) {
                    Text(text = entry.name)
                }
            }
        }

        SingleChoiceSegmentedButtonRow {
            LeaderboardPeriod.entries.forEach { entry ->
                SegmentedButton(
                    selected = currentPeriod == entry,
                    onClick = { onPeriodFilterChange(entry) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = entry.ordinal,
                        count = LeaderboardPeriod.entries.size
                    ),
                    colors = colors
                ) {
                    Text(text = entry.name)
                }
            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_NO)
@Preview(uiMode = UI_MODE_NIGHT_YES, name = "night mode")
@Composable
fun PreviewLeaderFilters() {
    WalkingForRochesterTheme {
        Surface {
            LeaderFilters(
                currentType = LeaderboardType.Collection,
                currentPeriod = LeaderboardPeriod.Week
            )
        }
    }
}

