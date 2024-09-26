package com.walkingforrochester.walkingforrochester.android.ui.composable.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@Composable
fun HorizontalNumberPicker(
    modifier: Modifier = Modifier,
    minValue: Int,
    maxValue: Int,
    defaultValue: Int,
    buttonSize: Dp = 48.dp,
    textStyle: TextStyle = MaterialTheme.typography.titleLarge,
    onValueChange: (Int) -> Unit
) {
    var currentValue by rememberSaveable { mutableIntStateOf(if (defaultValue > maxValue) maxValue else if (defaultValue < minValue) minValue else defaultValue) }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = {
                if (currentValue > minValue) {
                    onValueChange(--currentValue)
                }
            },
            enabled = currentValue > minValue
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                modifier = Modifier.size(buttonSize),
                contentDescription = "Decrease number"
            )
        }

        Text(text = "$currentValue", style = textStyle)

        IconButton(
            onClick = {
                if (currentValue < maxValue) {
                    onValueChange(++currentValue)
                }
            },
            enabled = currentValue < maxValue
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                modifier = Modifier.size(buttonSize),
                contentDescription = "Increase number"
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
                maxValue = 10,
                defaultValue = 100,
                onValueChange = {})
        }
    }
}