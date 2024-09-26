package com.walkingforrochester.walkingforrochester.android.ui.composable.logawalk

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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

@Composable
fun GuidelinesDialog(
    modifier: Modifier = Modifier,
    onLinkClick: () -> Unit = {},
    onAcceptGuideLines: () -> Unit = {},
    onDismissGuidelines: () -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current
    val guidelinesUrl = stringResource(R.string.guidelines_url)
    val waiverUrl = stringResource(id = R.string.waiver_url)

    WFRDialog(
        modifier = modifier,
        onDismissRequest = onDismissGuidelines,
        icon = { Icon(imageVector = Icons.Filled.Info, contentDescription = null) },
        title = { Text(text = stringResource(R.string.walk_dialog_title)) },
        buttons = {
            TextButton(
                onClick = onDismissGuidelines,
            ) {
                Text(text = stringResource(R.string.decline))
            }
            TextButton(
                onClick = onAcceptGuideLines,
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
                        onLinkClick()
                    },
                    text = stringResource(id = R.string.view_safety_guidelines),
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
                Text(
                    modifier = Modifier.clickable {
                        uriHandler.openUri(waiverUrl)
                        onLinkClick()
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
        Surface {
            GuidelinesDialog()
        }
    }
}