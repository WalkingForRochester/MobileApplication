package com.walkingforrochester.walkingforrochester.android.ui.modifier

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode

@Composable
fun Modifier.backgroundInPreview(color: Color): Modifier {
    return if (LocalInspectionMode.current) {
        this then Modifier.Companion.background(color)
    } else {
        this
    }
}