package com.walkingforrochester.walkingforrochester.android.ui.composable.logawalk

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.walkingforrochester.walkingforrochester.android.R
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

    AlertDialog(
        onDismissRequest = onDismissGuidelines,
        confirmButton = {
            TextButton(
                onClick = onAcceptGuideLines,
            ) {
                Text(text = stringResource(R.string.accept))
            }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDismissGuidelines) {
                Text(text = stringResource(R.string.decline))
            }
        },
        icon = { Icon(imageVector = Icons.Filled.Info, contentDescription = null) },
        title = {
            // Manually centering/breaking due to title length
            Text(
                text = stringResource(R.string.walk_dialog_title),
                textAlign = TextAlign.Center,
                style = LocalTextStyle.current.copy(lineBreak = LineBreak.Heading),
            )
        },
        text = {
            val linkStyles = TextLinkStyles(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                ),
                focusedStyle = SpanStyle(
                    background = LocalContentColor.current.copy(alpha = .1f)
                ),
                hoveredStyle = SpanStyle(
                    background = LocalContentColor.current.copy(alpha = .08f)
                ),
                pressedStyle = SpanStyle(
                    background = LocalContentColor.current.copy(alpha = .1f)
                )
            )

            val linkInteractionListener = LinkInteractionListener {
                val url = (it as LinkAnnotation.Url).url
                uriHandler.openUri(url)
                onLinkClick()
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.safety_guidelines_dialog)
                )

                Text(
                    text = buildAnnotatedString {
                        withLink(
                            LinkAnnotation.Url(
                                url = guidelinesUrl,
                                styles = linkStyles,
                                linkInteractionListener = linkInteractionListener
                            )
                        ) { append(stringResource(id = R.string.view_safety_guidelines)) }
                    }
                )
                Text(
                    text = buildAnnotatedString {
                        withLink(
                            LinkAnnotation.Url(
                                url = waiverUrl,
                                styles = linkStyles,
                                linkInteractionListener = linkInteractionListener
                            )
                        ) { append(stringResource(id = R.string.view_waiver)) }
                    }
                )
            }
        }
    )
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