package com.walkingforrochester.walkingforrochester.android.ui.composable.common

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@Composable
fun WFRDialog(
    modifier: Modifier = Modifier,
    contentTextStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    buttonsArrangement: Arrangement.Horizontal = Arrangement.End,
    dialogPadding: PaddingValues = PaddingValues(all = 24.dp),
    dialogProperties: DialogProperties = DialogProperties(),
    onDismissRequest: () -> Unit,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    buttons: @Composable RowScope.() -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = dialogProperties
    ) {
        Surface(
            modifier = modifier.wrapContentSize(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.0.dp
        ) {
            Column(modifier = Modifier.padding(dialogPadding)) {
                icon?.let {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.secondary) {
                        Box(
                            Modifier
                                .padding(bottom = 16.dp)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            icon()
                        }
                    }
                }
                title?.let {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                        ProvideTextStyle(value = MaterialTheme.typography.headlineSmall) {
                            Box(
                                // Align the title to the center when an icon is present.
                                Modifier
                                    .padding(bottom = 16.dp)
                                    .align(
                                        if (icon == null) {
                                            Alignment.Start
                                        } else {
                                            Alignment.CenterHorizontally
                                        }
                                    )
                            ) {
                                title()
                            }
                        }
                    }
                }
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                    ProvideTextStyle(value = contentTextStyle) {
                        Box(
                            Modifier
                                .weight(weight = 1f, fill = false)
                                .padding(bottom = 24.dp)
                                .align(Alignment.Start)
                        ) {
                            content()
                        }
                    }
                }
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.primary) {
                    ProvideTextStyle(value = MaterialTheme.typography.labelLarge) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = buttonsArrangement,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            buttons()
                        }
                    }
                }
            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Preview(showBackground = true)
@Composable
fun PreviewWFRDialog() {
    WalkingForRochesterTheme {
        WFRDialog(
            onDismissRequest = { /*TODO*/ },
            icon = { Icon(imageVector = Icons.Filled.Info, contentDescription = null) },
            title = { Text(text = stringResource(R.string.safety_guidelines)) },
            buttons = {
                TextButton(onClick = { /*TODO*/ }) {
                    Text("Cancel")
                }
                TextButton(onClick = { /*TODO*/ }) {
                    Text("Accept")
                }
            }) {
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
        }
    }
}