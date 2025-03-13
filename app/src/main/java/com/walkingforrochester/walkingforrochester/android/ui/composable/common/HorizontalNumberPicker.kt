package com.walkingforrochester.walkingforrochester.android.ui.composable.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.walkingforrochester.walkingforrochester.android.R
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@Composable
fun HorizontalNumberPicker(
    minValue: Int,
    maxValue: Int,
    modifier: Modifier = Modifier,
    defaultValue: Int = minValue,
    buttonSize: Dp = 32.dp,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
    onValueChange: (Int) -> Unit = {}
) {
    var currentValue by rememberSaveable { mutableIntStateOf(if (defaultValue > maxValue) maxValue else if (defaultValue < minValue) minValue else defaultValue) }

    Row(
        modifier = modifier.widthIn(min = 120.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                if (currentValue > minValue) {
                    onValueChange(--currentValue)
                }
            },
            enabled = currentValue > minValue
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowLeft,
                modifier = Modifier.size(buttonSize),
                contentDescription = stringResource(R.string.decrease_value)
            )
        }

        Text(
            text = "$currentValue",
            style = textStyle
        )

        IconButton(
            onClick = {
                if (currentValue < maxValue) {
                    onValueChange(++currentValue)
                }
            },
            enabled = currentValue < maxValue
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowRight,
                modifier = Modifier.size(buttonSize),
                contentDescription = stringResource(R.string.increase_value)
            )
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewHorizontalNumberPicker() {
    WalkingForRochesterTheme {
        Surface {
            HorizontalNumberPicker(
                minValue = 0,
                maxValue = 88,
                defaultValue = 78,
                onValueChange = {})
        }
    }
}