package com.walkingforrochester.walkingforrochester.android.ui.composable.leaderboard

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.walkingforrochester.walkingforrochester.android.ui.state.PeriodFilter
import com.walkingforrochester.walkingforrochester.android.ui.state.TypeFilter
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@Composable
fun LeaderFilters(
    modifier: Modifier = Modifier,
    onTypeFilterChange: (TypeFilter) -> Unit,
    onPeriodFilterChange: (PeriodFilter) -> Unit
) {
    Column(modifier = modifier) {
        SegmentedControl(
            items = TypeFilter.values().asList(),
            onItemSelection = onTypeFilterChange as (Any) -> Unit
        )
        SegmentedControl(
            items = PeriodFilter.values().asList(),
            onItemSelection = onPeriodFilterChange as (Any) -> Unit
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_NO)
@Preview(uiMode = UI_MODE_NIGHT_YES, name = "night mode")
@Composable
fun PreviewLeaderFilters() {
    WalkingForRochesterTheme {
        LeaderFilters(onTypeFilterChange = {}, onPeriodFilterChange = {})
    }
}

@Composable
fun SegmentedControl(
    items: List<Any>,
    defaultSelectedItemIndex: Int = 0,
    useFixedWidth: Boolean = false,
    itemWidth: Dp = 120.dp,
    cornerRadius: Int = 100,
    onItemSelection: (Any) -> Unit
) {
    val selectedIndex = remember { mutableStateOf(defaultSelectedItemIndex) }

    Row(
        modifier = Modifier
    ) {
        items.forEachIndexed { index, item ->
            OutlinedButton(
                modifier = when (index) {
                    0 -> {
                        if (useFixedWidth) {
                            Modifier
                                .width(itemWidth)
                                .offset(0.dp, 0.dp)
                                .zIndex(if (selectedIndex.value == index) 1f else 0f)
                        } else {
                            Modifier
                                .wrapContentSize()
                                .offset(0.dp, 0.dp)
                                .zIndex(if (selectedIndex.value == index) 1f else 0f)
                        }
                    }

                    else -> {
                        if (useFixedWidth)
                            Modifier
                                .width(itemWidth)
                                .offset((-1 * index).dp, 0.dp)
                                .zIndex(if (selectedIndex.value == index) 1f else 0f)
                        else Modifier
                            .wrapContentSize()
                            .offset((-1 * index).dp, 0.dp)
                            .zIndex(if (selectedIndex.value == index) 1f else 0f)
                    }
                },
                onClick = {
                    selectedIndex.value = index
                    onItemSelection(item)
                },
                shape = when (index) {
                    /**
                     * left outer button
                     */
                    0 -> RoundedCornerShape(
                        topStartPercent = cornerRadius,
                        topEndPercent = 0,
                        bottomStartPercent = cornerRadius,
                        bottomEndPercent = 0
                    )
                    /**
                     * right outer button
                     */
                    items.size - 1 -> RoundedCornerShape(
                        topStartPercent = 0,
                        topEndPercent = cornerRadius,
                        bottomStartPercent = 0,
                        bottomEndPercent = cornerRadius
                    )
                    /**
                     * middle button
                     */
                    else -> RoundedCornerShape(
                        topStartPercent = 0,
                        topEndPercent = 0,
                        bottomStartPercent = 0,
                        bottomEndPercent = 0
                    )
                },
                border = BorderStroke(
                    1.dp, if (selectedIndex.value == index) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
                    }
                ),
                colors = if (selectedIndex.value == index) {
                    /**
                     * selected colors
                     */
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                } else {
                    /**
                     * not selected colors
                     */
                    ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                },
            ) {
                Text(
                    text = item.toString(),
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
