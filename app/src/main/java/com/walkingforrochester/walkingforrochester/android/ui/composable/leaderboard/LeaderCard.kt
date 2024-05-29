package com.walkingforrochester.walkingforrochester.android.ui.composable.leaderboard

import android.text.format.DateUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.walkingforrochester.walkingforrochester.android.model.Leader
import com.walkingforrochester.walkingforrochester.android.roundDouble
import com.walkingforrochester.walkingforrochester.android.ui.state.TypeFilter
import com.walkingforrochester.walkingforrochester.android.ui.theme.LeaderboardBronze
import com.walkingforrochester.walkingforrochester.android.ui.theme.LeaderboardGold
import com.walkingforrochester.walkingforrochester.android.ui.theme.LeaderboardSilver
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@Composable
fun LeaderCard(modifier: Modifier = Modifier, leader: Leader, type: TypeFilter) {
    Card(
        modifier = modifier.wrapContentHeight(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (leader.place) {
                1L -> LeaderboardGold
                2L -> LeaderboardSilver
                3L -> LeaderboardBronze
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Text(
                leader.place.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = when (leader.place) {
                    1L, 2L, 3L -> Color.Black
                    else -> MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier
                    .weight(0.2f)
                    .padding(8.dp)
            )
            if (leader.imgUrl.isNullOrBlank()) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "Profile Pic",
                    modifier = Modifier.size(64.dp)
                )
            } else {
                Image(
                    painter = rememberAsyncImagePainter(leader.imgUrl),
                    contentDescription = stringResource(com.walkingforrochester.walkingforrochester.android.R.string.profile_pic),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                )
            }
            Column(
                Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text(
                    (if (leader.nickname.isNullOrBlank()) leader.firstName else leader.nickname)
                        ?: "",
                    style = MaterialTheme.typography.labelLarge,
                    color = when (leader.place) {
                        1L, 2L, 3L -> Color.Black
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                /*Text(
                    "${leader.accountId}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = when (leader.place) {
                            1L, 2L, 3L -> Color.Black
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        fontWeight = FontWeight.Light
                    )
                )*/
            }
            Text(
                text = when (type) {
                    TypeFilter.Collection -> leader.collection.toString()
                    TypeFilter.Distance -> "${roundDouble(leader.distance)} mi"
                    TypeFilter.Duration -> DateUtils.formatElapsedTime(
                        (leader.duration ?: 0) / 1000
                    )
                },
                style = MaterialTheme.typography.labelLarge,
                color = when (leader.place) {
                    1L, 2L, 3L -> Color.Black
                    else -> MaterialTheme.colorScheme.onSurface
                }, modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun PreviewLeaderCard() {
    WalkingForRochesterTheme {
        //LeaderCard(leader = Leader(id = 337, place = 1, name = "John Smith", collected = 13, distance = 3.3, duration = 7200))
    }
}