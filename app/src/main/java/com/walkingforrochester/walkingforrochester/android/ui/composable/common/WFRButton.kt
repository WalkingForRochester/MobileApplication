package com.walkingforrochester.walkingforrochester.android.ui.composable.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@Composable
fun WFRButton(
    onClick: () -> Unit,
    @StringRes label: Int,
    modifier: Modifier = Modifier,
    buttonColor: Color = MaterialTheme.colorScheme.inverseSurface,
    labelColor: Color = MaterialTheme.colorScheme.inverseOnSurface,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val defaultPadding = ButtonDefaults.ContentPadding
    val contentPadding = PaddingValues(
        start = defaultPadding.calculateStartPadding(LayoutDirection.Ltr) + 12.dp,
        top = defaultPadding.calculateTopPadding() + 4.dp,
        end = defaultPadding.calculateEndPadding(LayoutDirection.Ltr) + 12.dp,
        bottom = defaultPadding.calculateBottomPadding() + 4.dp
    )

    Button(
        onClick = { if (!loading) onClick() },
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = labelColor
        ),
        enabled = enabled,
        modifier = modifier,
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

@Preview
@Composable
fun WFRButtonPreview() {
    WalkingForRochesterTheme {
        Surface {
            Box(
                Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                WFRButton(
                    onClick = { /*TODO*/ },
                    label = R.string.sign_in,
                    loading = true
                )
            }
        }
    }
}