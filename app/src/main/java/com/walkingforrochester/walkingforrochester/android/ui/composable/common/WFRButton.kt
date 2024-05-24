package com.walkingforrochester.walkingforrochester.android.ui.composable.common

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
    buttonColor: Color = MaterialTheme.colorScheme.inverseSurface,
    labelColor: Color = MaterialTheme.colorScheme.inverseOnSurface,
    onClick: () -> Unit,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Button(
        onClick = { if (!loading) onClick() },
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = labelColor
        ),
        enabled = enabled,
        modifier = modifier.height(52.dp)
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
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}