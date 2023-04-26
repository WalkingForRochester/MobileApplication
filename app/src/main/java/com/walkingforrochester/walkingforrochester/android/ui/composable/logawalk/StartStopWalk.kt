package com.walkingforrochester.walkingforrochester.android.ui.composable.logawalk

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@Composable
fun StartStopWalkButton(
    walking: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(onClick = onClick, modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(if (walking) R.drawable.ic_finish else R.drawable.ic_walk),
                contentDescription = "Start/Stop Walk"
            )
            Text(text = if (walking) "Stop" else "Start")
        }
    }
}

@Preview(showBackground = true, widthDp = 150)
@Composable
fun PreviewStartStopWalkButton() {
    WalkingForRochesterTheme {
        StartStopWalkButton(walking = false, onClick = { /*TODO*/ })
    }
}