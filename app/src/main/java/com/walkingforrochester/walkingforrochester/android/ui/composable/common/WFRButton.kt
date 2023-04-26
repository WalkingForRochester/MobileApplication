package com.walkingforrochester.walkingforrochester.android.ui.composable.common

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun WFRButton(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    @StringRes label: Int,
    buttonColor: Color = Color.Black,
    labelColor: Color = Color.White,
    onClick: () -> Unit,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    ElevatedButton(
        onClick = { if (!loading) onClick() },
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = buttonColor,
            contentColor = labelColor
        ),
        enabled = enabled,
        modifier = modifier.height(54.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (loading) {
                CircularProgressIndicator(
                    modifier.size(27.dp),
                    color = labelColor
                )
            }
            Row(
                modifier = if (loading) Modifier.alpha(0f) else Modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = stringResource(label),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}