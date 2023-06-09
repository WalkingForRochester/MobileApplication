package com.walkingforrochester.walkingforrochester.android.ui.composable.logawalk

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.composable.common.WFRDialog
import com.walkingforrochester.walkingforrochester.android.ui.theme.MaterialGreen
import com.walkingforrochester.walkingforrochester.android.ui.theme.MaterialRed
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
                Text(text = stringResource(R.string.i_decline), color = MaterialRed)
            }
            TextButton(
                onClick = logAWalkViewModel::onAcceptGuidelines,
            ) {
                Text(text = stringResource(R.string.i_agree), color = MaterialGreen)
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
                    text = "View Safety Guidelines in browser",
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
                Text(
                    modifier = Modifier.clickable {
                        uriHandler.openUri(waiverUrl)
                        logAWalkViewModel.onGuidelinesLinkClick()
                    },
                    text = "View Waiver in browser",
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
                        text = "View Safety Guidelines in browser",
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