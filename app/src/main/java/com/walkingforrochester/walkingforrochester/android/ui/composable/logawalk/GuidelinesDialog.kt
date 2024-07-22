package com.walkingforrochester.walkingforrochester.android.ui.composable.logawalk

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRDialog
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme
import com.walkingforrochester.walkingforrochester.android.viewmodel.LogAWalkViewModel

@Composable
fun GuidelinesDialog(
    modifier: Modifier = Modifier,
    logAWalkViewModel: LogAWalkViewModel
) {
    val uriHandler = LocalUriHandler.current
    val guidelinesUrl = stringResource(R.string.guidelines_url)
    val waiverUrl = stringResource(id = R.string.waiver_url)

    WFRDialog(
        modifier = modifier,
        onDismissRequest = logAWalkViewModel::onDismissGuidelinesDialog,
        icon = { Icon(imageVector = Icons.Filled.Info, contentDescription = null) },
        title = { Text(text = stringResource(R.string.walk_dialog_title)) },
        buttons = {
            TextButton(
                onClick = logAWalkViewModel::onDismissGuidelinesDialog,
            ) {
                Text(text = stringResource(R.string.decline))
            }
            TextButton(
                onClick = logAWalkViewModel::onAcceptGuidelines,
            ) {
                Text(text = stringResource(R.string.accept))
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.safety_guidelines_dialog)
            )
                Text(
                    modifier = Modifier.clickable {
                        uriHandler.openUri(guidelinesUrl)
                        logAWalkViewModel.onGuidelinesLinkClick()
                    },
                    text = stringResource(id = R.string.view_safety_guidelines),
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
                Text(
                    modifier = Modifier.clickable {
                        uriHandler.openUri(waiverUrl)
                        logAWalkViewModel.onGuidelinesLinkClick()
                    },
                    text = stringResource(id = R.string.view_waiver),
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Preview
@Composable
fun PreviewGuidelinesDialog() {
    WalkingForRochesterTheme {
        AlertDialog(onDismissRequest = { /*TODO*/ },
            icon = { Icon(imageVector = Icons.Filled.Info, contentDescription = null) },
            title = { Text(text = stringResource(R.string.safety_guidelines)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.safety_guidelines_dialog)
                    )
                    Text(
                        text = stringResource(id = R.string.view_safety_guidelines),
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {},
                    enabled = true
                ) {
                    Text(stringResource(R.string.accept))
                }
            }
        )
    }
}