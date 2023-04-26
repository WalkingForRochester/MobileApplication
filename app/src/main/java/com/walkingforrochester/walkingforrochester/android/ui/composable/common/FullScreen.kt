package com.walkingforrochester.walkingforrochester.android.ui.composable.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun FullScreen(content: @Composable () -> Unit) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        content()
    }
}