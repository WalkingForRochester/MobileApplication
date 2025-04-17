package com.walkingforrochester.walkingforrochester.android.ui.composable.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WFRButton(
    onClick: () -> Unit,
    @StringRes label: Int,
    modifier: Modifier = Modifier,
    testTag: String = "",
    enabled: Boolean = true,
    loading: Boolean = false,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding
) {
    Button(
        onClick = { if (!loading) onClick() },
        modifier = modifier.semantics {
            if (testTag.isNotBlank()) {
                testTagsAsResourceId = true
                this.testTag = testTag
            }
        },
        enabled = enabled,
        colors = colors,
        contentPadding = contentPadding

    ) {
        if (loading) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier.size(ButtonDefaults.IconSize),
                    color = LocalContentColor.current
                )
                Text(
                    text = stringResource(label),
                    color = Color.Transparent
                )
            }
        } else {
            Text(
                text = stringResource(label),
            )
        }
    }
}

@Composable
fun WFROutlinedButton(
    onClick: () -> Unit,
    @StringRes label: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding
) {
    OutlinedButton(
        onClick = { if (!loading) onClick() },
        enabled = enabled,
        modifier = modifier,
        colors = colors,
        contentPadding = contentPadding

    ) {
        if (loading) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier.size(ButtonDefaults.IconSize),
                    color = LocalContentColor.current
                )
                Text(
                    text = stringResource(label),
                    color = Color.Transparent
                )
            }
        } else {
            Text(
                text = stringResource(label),
            )
        }
    }
}

object WFRButtonDefaults {
    val wideContentPadding = PaddingValues(
        start = ButtonDefaults.ContentPadding.calculateStartPadding(LayoutDirection.Ltr) + 12.dp,
        end = ButtonDefaults.ContentPadding.calculateEndPadding(LayoutDirection.Ltr) + 12.dp,
    )
}

@Preview
@Composable
fun WFRButtonPreview() {
    WalkingForRochesterTheme {
        Surface {
            Box(
                Modifier
                    .width(200.dp)
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                WFRButton(
                    onClick = { /*TODO*/ },
                    label = R.string.sign_in,
                    loading = false
                )
            }
        }
    }
}

@Preview
@Composable
fun WFROutlinedButtonPreview() {
    WalkingForRochesterTheme {
        Surface {
            Box(
                Modifier
                    .width(200.dp)
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                WFROutlinedButton(
                    onClick = {},
                    label = R.string.cancel,
                    loading = false
                )
            }
        }
    }
}