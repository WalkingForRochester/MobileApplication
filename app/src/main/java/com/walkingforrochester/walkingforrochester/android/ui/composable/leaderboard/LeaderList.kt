package com.walkingforrochester.walkingforrochester.android.ui.composable.leaderboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.walkingforrochester.walkingforrochester.android.model.Leader
import com.walkingforrochester.walkingforrochester.android.ui.state.TypeFilter
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@Composable
fun LeaderList(
    modifier: Modifier = Modifier,
    leaders: List<Leader>,
    type: TypeFilter
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 8.dp)
    ) {
        items(leaders) { leader ->
            LeaderCard(leader = leader, type = type)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLeaderList() {
    WalkingForRochesterTheme {
        /*LeaderList(leaders = List(100) { index ->
            Leader(
                id = Random.nextLong(1000000, 1000200),
                place = index.toLong() + 1,
                name = "John Smith $index",
                collected = 100 - index,
                distance = (100 - index).toDouble(),
                duration = (10000 - index).toLong()
            )
        })*/
    }
}