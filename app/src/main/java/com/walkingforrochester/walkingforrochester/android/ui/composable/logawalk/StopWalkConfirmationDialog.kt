package com.walkingforrochester.walkingforrochester.android.ui.composable.logawalk

import android.content.res.Configuration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@Composable
fun StopWalkConfirmationDialog(
    modifier: Modifier = Modifier,
    onStopWalk: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onStopWalk,
            ) {
                Text(text = stringResource(R.string.stop_walk_button))
            }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.continue_walk_button))
            }
        },
        title = {
            // Manually centering/breaking due to title length
            Text(
                text = stringResource(R.string.stop_walk_title),
                textAlign = TextAlign.Center,
                style = LocalTextStyle.current.copy(lineBreak = LineBreak.Heading),
            )
        },
        text = {
            Text(text = stringResource(R.string.stop_walk_message))
        }
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewStopWalkConfirmationDialog() {
    WalkingForRochesterTheme {
        Surface {
            StopWalkConfirmationDialog()
        }
    }
}