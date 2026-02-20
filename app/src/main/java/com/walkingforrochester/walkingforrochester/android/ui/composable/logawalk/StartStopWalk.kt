package com.walkingforrochester.walkingforrochester.android.ui.composable.logawalk

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@Composable
fun StartStopWalkButton(
    walking: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(64.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val iconResId =
                if (walking) R.drawable.ic_sports_score_24dp else R.drawable.ic_directions_walk_24dp
            Icon(
                painter = painterResource(iconResId),
                contentDescription = null
            )
            Text(
                text = stringResource(if (walking) R.string.stop_button else R.string.start_button)
            )
        }
    }
}

@Preview
@Composable
fun PreviewStartStopWalkButton() {
    WalkingForRochesterTheme {
        Surface {
            Box(modifier = Modifier.padding(100.dp)) {
                StartStopWalkButton(walking = false, onClick = {})
            }
        }
    }
}