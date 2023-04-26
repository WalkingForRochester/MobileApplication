package com.walkingforrochester.walkingforrochester.android.ui.composable.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.walkingforrochester.walkingforrochester.android.ui.theme.WalkingForRochesterTheme

@Composable
fun CommunityServiceCheckbox(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    labelColor: Color = Color.Black,
    checkmarkColor: Color = Color.White
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier.weight(0.95f),
            text = "Check this box if you need community service hours and documentation",
            color = labelColor
        )
        Checkbox(
            modifier = Modifier.weight(0.05f),
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = labelColor,
                uncheckedColor = labelColor,
                checkmarkColor = checkmarkColor
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCommunityServiceCheckbox() {
    WalkingForRochesterTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            CommunityServiceCheckbox(checked = true, onCheckedChange = {})
        }
    }
}