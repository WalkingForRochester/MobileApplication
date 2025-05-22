package com.walkingforrochester.walkingforrochester.android.ui.composable.leaderboard

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.walkingforrochester.walkingforrochester.android.formatDouble
import com.walkingforrochester.walkingforrochester.android.formatElapsedMilli
import com.walkingforrochester.walkingforrochester.android.model.Leader
import com.walkingforrochester.walkingforrochester.android.model.LeaderboardType
import com.walkingforrochester.walkingforrochester.android.ui.theme.LeaderboardBronze
import com.walkingforrochester.walkingforrochester.android.ui.theme.LeaderboardGold
import com.walkingforrochester.walkingforrochester.android.ui.theme.LeaderboardSilver
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@Composable
fun LeaderCard(
    leader: Leader,
    index: Int,
    type: LeaderboardType,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 8.dp),

        colors = CardDefaults.cardColors(
            containerColor = when (index) {
                0 -> LeaderboardGold
                1 -> LeaderboardSilver
                2 -> LeaderboardBronze
                else -> MaterialTheme.colorScheme.surfaceContainerHighest
            },
            contentColor = when (index) {
                in 0..2 -> Color.Black
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "${index + 1}",
                modifier = Modifier.padding(start = 8.dp, end = 16.dp),
                style = MaterialTheme.typography.labelLarge,
            )
            if (leader.imgUrl.isBlank()) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = stringResource(com.walkingforrochester.walkingforrochester.android.R.string.profile_pic),
                    modifier = Modifier.size(64.dp),
                )
            } else {
                AsyncImage(
                    model = leader.imgUrl,
                    error = rememberVectorPainter(image = Icons.Filled.AccountCircle),
                    contentDescription = stringResource(com.walkingforrochester.walkingforrochester.android.R.string.profile_pic),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                )
            }

            Text(
                text = leader.nickname.ifBlank { leader.firstName },
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .weight(1f),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = when (type) {
                    LeaderboardType.Collection -> leader.collection.toString()
                    LeaderboardType.Distance -> "${leader.distance.formatDouble()} mi"
                    LeaderboardType.Duration -> leader.duration.formatElapsedMilli()
                },
                modifier = Modifier.padding(horizontal = 8.dp),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(locale = "fr")
@Composable
fun PreviewLeaderCard() {
    WalkingForRochesterTheme {
        LeaderCard(
            leader = Leader(
                collectionPosition = 1,
                accountId = 2,
                firstName = "John",
                nickname = "",
                imgUrl = "",
                collection = 13L,
                distance = 3.3,
                duration = 90200L
            ),
            index = 2,
            type = LeaderboardType.Distance
        )
    }
}